package io.github.petty.vision.adapter.in;

import io.github.petty.vision.helper.ImageValidator;
import io.github.petty.vision.helper.ImageValidator.ValidationResult;
import io.github.petty.vision.port.in.VisionUseCase;
import io.github.petty.vision.service.VisionServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import jakarta.servlet.http.HttpSession;
import java.io.IOException;

@Slf4j
@Controller
@RequestMapping("/vision")
@RequiredArgsConstructor
public class VisionController {
    private final VisionUseCase vision;
    private final VisionServiceImpl visionService;
    private final ImageValidator imageValidator;

    @GetMapping("/upload")
    public String page() {
        return "visionUpload";
    }

    @PostMapping("/species")
    @ResponseBody
    public String getSpeciesInterim(
            @RequestParam("file") MultipartFile file,
            @RequestParam("petName") String petName,
            HttpSession session
    ) throws IOException {
        // 파일 유효성 검사
        ValidationResult vr = imageValidator.validate(file);
        if (!vr.isValid()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, vr.getMessage());
        }

        // UnifiedFlowController와 호환되도록 세션에 데이터 저장
        session.setAttribute("petName", petName);

        // 파일을 임시로 저장 (나중에 analyze에서 사용)
        try {
            byte[] imageBytes = file.getBytes();
            session.setAttribute("tempImageBytes", imageBytes);

            // 이미지를 Base64로 인코딩하여 세션에 저장
            String imageBase64 = java.util.Base64.getEncoder().encodeToString(imageBytes);
            session.setAttribute("petImageBase64", "data:image/jpeg;base64," + imageBase64);
        } catch (IOException e) {
            log.warn("이미지 저장 실패", e);
        }

        // 기존 서비스 호출
        return vision.interim(file.getBytes(), petName);
    }

    @PostMapping("/analyze")
    @ResponseBody
    public String analyze(
            @RequestParam("file") MultipartFile file,
            @RequestParam("petName") String petName,
            HttpSession session
    ) {
        // 파일 유효성 검사
        ValidationResult vr = imageValidator.validate(file);
        if (!vr.isValid()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, vr.getMessage());
        }

        try {
            // Vision 분석 결과 생성
            String visionReport = visionService.analyze(file, petName);

            // UnifiedFlowController와 호환되도록 세션에 결과 저장
            session.setAttribute("visionReport", visionReport);
            session.setAttribute("petName", petName);

            log.info("✅ Vision 분석 완료 - petName: {}, reportLength: {}",
                    petName, visionReport != null ? visionReport.length() : 0);

            return visionReport;
        } catch (Exception e) {
            log.error("❌ Vision 분석 실패", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "분석 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
}