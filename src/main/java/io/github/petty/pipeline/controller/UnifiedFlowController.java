package io.github.petty.pipeline.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.petty.llm.dto.RecommendResponseDTO;
import io.github.petty.llm.service.RecommendService;
import io.github.petty.pipeline.support.TogetherPromptBuilder;
import io.github.petty.vision.port.in.VisionUseCase;
import io.github.petty.vision.service.VisionServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpSession;
import java.util.Map;

@Slf4j
@Controller
@RequestMapping("/flow")
@RequiredArgsConstructor
public class UnifiedFlowController {

    private final VisionUseCase visionUseCase;
    private final VisionServiceImpl visionService;
    private final TogetherPromptBuilder togetherPromptBuilder;
    private final RecommendService recommendService;

    // 1. 반려동물 분석 페이지 (초기 진입)
    @GetMapping("/analyze")
    public String analyzePage() {
        return "analyze"; // analyze.html 반환
    }

    // 2. '내 반려동물 분석하기' 버튼 클릭 시
    @PostMapping("/analyze")
    public String performAnalysis(
            @RequestParam("file") MultipartFile file,
            @RequestParam("petName") String petName,
            RedirectAttributes redirectAttributes,
            HttpSession session
    ) {
        try {
            String interim = visionUseCase.interim(file.getBytes(), petName);
            String visionReport = visionService.analyze(file, petName);

            // 다음 페이지로 flash attribute로 전달
            redirectAttributes.addFlashAttribute("interim", interim);
            redirectAttributes.addFlashAttribute("petName", petName);

            // 최종 visionReport는 세션에 저장하여 다음 단계에서 재사용
            session.setAttribute("visionReport", visionReport);
            session.setAttribute("petName", petName);

            // 이미지를 Base64로 인코딩하여 세션에 저장
            try {
                byte[] imageBytes = file.getBytes();
                String imageBase64 = java.util.Base64.getEncoder().encodeToString(imageBytes);
                session.setAttribute("petImageBase64", "data:image/jpeg;base64," + imageBase64);
            } catch (Exception e) {
                log.warn("이미지 저장 실패", e);
            }

            return "redirect:/flow/showInterimLoading";
        } catch (Exception e) {
            log.error("❌ 반려동물 분석 중 오류", e);
            redirectAttributes.addFlashAttribute("error", "반려동물 분석 중 오류 발생: " + e.getMessage());
            return "redirect:/flow/analyze";
        }
    }

    // 3. 중간 분석 로딩 페이지
    @GetMapping("/showInterimLoading")
    public String showInterimLoading(
            @ModelAttribute("interim") String interim,
            @ModelAttribute("petName") String petName,
            Model model) {

        if (interim == null || interim.isEmpty()) {
            model.addAttribute("interim", "데이터를 불러오는 중이거나, 이전 요청이 완료되지 않았습니다. 잠시만 기다려 주세요.");
        }
        model.addAttribute("petName", petName);

        return "interim_loading";
    }

    // 4. 최종 Vision 보고서 페이지 - 세션 기반으로 수정
    @GetMapping("/showVisionReport")
    public String showVisionReport(Model model, HttpSession session) {
        String visionReport = (String) session.getAttribute("visionReport");
        String petName = (String) session.getAttribute("petName");
        String petImageBase64 = (String) session.getAttribute("petImageBase64");

        if (visionReport == null) {
            log.warn("⚠️ 세션에 Vision 보고서가 없습니다");
            model.addAttribute("error", "세션에 Vision 보고서가 없습니다. 다시 분석을 시작해 주세요.");
            return "visionUpload"; // vision 업로드 페이지로 이동
        }

        model.addAttribute("visionReport", visionReport);
        model.addAttribute("petName", petName);
        if (petImageBase64 != null) {
            model.addAttribute("petImageUrl", petImageBase64);
        }

        log.info("✅ Vision 보고서 표시 - petName: {}, reportLength: {}",
                petName, visionReport.length());

        return "vision_report";
    }

    // 5. '여행지 추천 받기' 버튼 클릭 시
    @PostMapping("/report")
    public String generateRecommendation(
            @RequestParam("petName") String petName,
            @RequestParam("location") String location,
            @RequestParam("info") String info,
            RedirectAttributes redirectAttributes,
            HttpSession session
    ) {
        try {
            String visionReport = (String) session.getAttribute("visionReport");
            if (visionReport == null) {
                log.warn("⚠️ 세션에 Vision 보고서가 없습니다 - petName: {}", petName);
                redirectAttributes.addFlashAttribute("error", "세션에 Vision 보고서가 없습니다. 다시 분석을 시작해 주세요.");
                return "redirect:/flow/analyze";
            }

            // 프롬프트 빌딩 및 추천 서비스 호출
            String jsonPrompt = togetherPromptBuilder.buildPrompt(visionReport, location, info);
            Map<String, String> promptMapper = new ObjectMapper().readValue(jsonPrompt, new TypeReference<>() {});
            RecommendResponseDTO recommendation = recommendService.recommend(promptMapper);

            // 추천 결과는 세션에 저장
            session.setAttribute("recommendationResult", recommendation);

            // 다음 페이지로 필요한 데이터 전달
            redirectAttributes.addFlashAttribute("petName", petName);
            redirectAttributes.addFlashAttribute("location", location);
            redirectAttributes.addFlashAttribute("info", info);

            log.info("✅ 추천 생성 완료 - petName: {}, location: {}", petName, location);

            return "redirect:/flow/showRecommendLoading";
        } catch (Exception e) {
            log.error("❌ 추천 생성 중 오류", e);
            redirectAttributes.addFlashAttribute("error", "추천 생성 중 오류 발생: " + e.getMessage());
            return "redirect:/flow/showVisionReport";
        }
    }

    // 6. 여행지 추천 로딩 페이지
    @GetMapping("/showRecommendLoading")
    public String showRecommendLoading(
            @ModelAttribute("petName") String petName,
            @ModelAttribute("location") String location,
            @ModelAttribute("info") String info,
            Model model) {

        model.addAttribute("petName", petName);
        model.addAttribute("location", location);
        model.addAttribute("info", info);
        return "recommend_loading";
    }

    // 7. 추천 여행지 결과 페이지
    @GetMapping("/showRecommendationResult")
    public String showRecommendationResult(Model model, HttpSession session) {
        RecommendResponseDTO recommendation = (RecommendResponseDTO) session.getAttribute("recommendationResult");

        if (recommendation == null) {
            log.warn("⚠️ 세션에 추천 여행지 결과가 없습니다");
            model.addAttribute("error", "세션에 추천 여행지 결과가 없습니다. 다시 시도해 주세요.");
            return "visionUpload";
        }

        model.addAttribute("recommendation", recommendation);
        model.addAttribute("recommendationResponse", recommendation); // 템플릿 호환성

        // 사용 후 세션에서 제거 (선택 사항)
        session.removeAttribute("recommendationResult");
        session.removeAttribute("visionReport");

        log.info("✅ 추천 결과 표시 완료");

        return "recommendation_result";
    }
}