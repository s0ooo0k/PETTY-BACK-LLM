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

    @GetMapping
    public String page() {
        return "unifiedFlow";
    }

    @PostMapping("/analyze")
    public String analyze(
            @RequestParam("file") MultipartFile file,
            @RequestParam("petName") String petName,
            Model model,
            HttpSession session
    ) {
        try {
            String interim = visionUseCase.interim(file.getBytes(), petName);
            String visionReport = visionService.analyze(file, petName);

            model.addAttribute("interim", interim);
            model.addAttribute("visionReport", visionReport);
            model.addAttribute("petName", petName);

            session.setAttribute("visionReport", visionReport);
        } catch (Exception e) {
            log.error("❌ interim 분석 중 오류", e);
            model.addAttribute("error", "중간 분석 중 오류 발생");
        }
        return "unifiedFlow";
    }

    @PostMapping("/report")
    public String report(
            @RequestParam("petName") String petName,
            @RequestParam("location") String location,
            @RequestParam("info") String info,
            Model model,
            HttpSession session
    ) {
        try {
            String visionReport = (String) session.getAttribute("visionReport");
            if (visionReport == null) {
                model.addAttribute("error", "세션에 Vision 보고서가 없습니다. 다시 분석을 시작해 주세요.");
                return "unifiedFlow";
            }

            String jsonPrompt = togetherPromptBuilder.buildPrompt(visionReport, location, info);
            Map<String, String> promptMapper = new ObjectMapper().readValue(jsonPrompt, new TypeReference<>() {});
            RecommendResponseDTO recommendation = recommendService.recommend(promptMapper);

            model.addAttribute("visionReport", visionReport); // 다시 보여주기 위해 필요
            model.addAttribute("recommendation", recommendation);
            model.addAttribute("petName", petName);
        } catch (Exception e) {
            log.error("❌ 추천 생성 중 오류", e);
            model.addAttribute("error", "추천 생성 중 오류 발생");
        }
        return "unifiedFlow";
    }
}
