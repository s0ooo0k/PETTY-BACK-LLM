package io.github.petty.pipeline.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.petty.llm.service.RecommendService;
import io.github.petty.pipeline.support.TogetherPromptBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

@Slf4j
@Controller
@RequiredArgsConstructor
public class PipelineController {

    private final TogetherPromptBuilder togetherPromptBuilder;
    private final RecommendService recommendService;

    @GetMapping("/pipeline")
    public String showPipelineForm() {
        return "pipeline";
    }

    @PostMapping("/pipeline")
    public String processPipeline(
            // form 으로 받은 임시 Vision 보고서
            @RequestParam("visionReport") String visionReport,
            // form 으로 받은 사용자 위치
            @RequestParam("location") String location,
            Model model
    ) {
        try {
//            String prompt = togetherPromptBuilder.buildPrompt(visionReport, location);
            String jsonPrompt = togetherPromptBuilder.buildPrompt(visionReport, location);
            log.info(jsonPrompt);
            ObjectMapper objectMapper = new ObjectMapper();
            Map<String, String> promptMapper = objectMapper.readValue(jsonPrompt, new TypeReference<>() {});
            log.info(promptMapper.toString());
            String prompt = recommendService.recommend(promptMapper);
            model.addAttribute("recommendation", prompt);
            return "pipeline";

        } catch (Exception e) {
            log.error("프롬프트 제작 중 오류 발생", e);
            model.addAttribute("error", "결과를 받아오지 못했습니다.");
            return "pipeline";
        }
    }
}
