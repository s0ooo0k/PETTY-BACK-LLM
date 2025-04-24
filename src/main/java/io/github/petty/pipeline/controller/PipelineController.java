package io.github.petty.pipeline.controller;

import io.github.petty.pipeline.support.TogetherPromptBuilder;
import io.github.petty.vision.service.VisionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Controller
@RequiredArgsConstructor
public class PipelineController {

    private final TogetherPromptBuilder togetherPromptBuilder;
    private final VisionService visionService;

    @GetMapping("/pipeline")
    public String showPipelineForm() {
        return "pipeline";
    }

    // 📌 필독

    // Vision과 통합 후 테스트 시 : finalProcessPipeline
    // Vision과 통합 전 테스트 시 : processPipeline

    // 통합이 완료되면 processPipeline은 필요 없습니다.
    // 테스트를 진행 할 때 위 내용을 참고 후 조건에 맞지 않는 메서드는 주석처리를 해야합니다.
    // ex) Vision과 통합 전이면 finalProcessPipeline 메서드를 주석처리

    // Form 으로 받은 임시 보고서 사용
    @PostMapping("/pipeline")
    public String processPipeline(
            // form 으로 받은 사용자 위치
            @RequestParam("location") String location,
            // form 으로 받은 임시 Vision 보고서
            @RequestParam("visionReport") String visionReport,
            Model model
    ) {
        try {
            String prompt = togetherPromptBuilder.buildPrompt(visionReport, location);

            model.addAttribute("recommendation", prompt);
            return "pipeline";

        } catch (Exception e) {
            log.error("프롬프트 제작 중 오류 발생", e);
            model.addAttribute("error", "결과를 받아오지 못했습니다.");
            return "pipeline";
        }
    }

    // 실제 Vision 보고서를 사용
    @PostMapping("/pipeline")
    public String finalProcessPipeline(
            // form 으로 받은 사용자 위치
            @RequestParam("location") String location,

            // VisionService 에서 필요한 요소들
            // VisionService와 동일하게 작성됨
            @RequestParam("file") MultipartFile file,
            @RequestParam("petName") String petName,
            Model model
    ) {
        try {
            String visionReport = visionService.createFinalReport(file, petName);
            String prompt = togetherPromptBuilder.buildPrompt(visionReport, location);

            model.addAttribute("recommendation", prompt);
            return "pipeline";

        } catch (Exception e) {
            log.error("프롬프트 제작 중 오류 발생", e);
            model.addAttribute("error", "결과를 받아오지 못했습니다.");
            return "pipeline";
        }
    }
}
