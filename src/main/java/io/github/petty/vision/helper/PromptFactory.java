package io.github.petty.vision.helper;

import io.github.petty.vision.config.VisionProperties;
import io.github.petty.vision.dto.gemini.GeminiRequest;
import io.github.petty.vision.dto.together.TogetherRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import java.util.*;
import java.util.Base64;

/**
 * 기존 PromptFactory에 최적화된 프롬프트 기능을 통합한 클래스
 */
@Component
@RequiredArgsConstructor
public class PromptFactory {
    private final VisionProperties prop;

    /* ---------- 공통 ---------- */
    /**
     * 중간 분석 결과 메시지 생성
     * @param pet 반려동물 이름
     * @param sp 감지된 종 (한글 또는 "알 수 없음")
     */
    public String interimMsg(String pet, String sp) {
        if ("알 수 없음".equals(sp)) {
            return String.format("'%s'에 대해서 알아볼게요! \n잠시만 기다려 주세요. 보고서를 작성 중입니다...", pet);
        } else {
            return String.format("오 '%s'는 '%s'이군요!\n잠시만 기다려 주세요. 보고서를 작성 중입니다...", pet, sp);
        }
    }

    /* ---------- Gemini ---------- */
    /**
     * Gemini API용 요청 생성
     */
    public GeminiRequest toGeminiReq(byte[] img, String pet, String sp) {
        String base64 = Base64.getEncoder().encodeToString(img);
        String promptText = detailedPrompt(pet, sp);
        Map<String, Object> part1 = Map.of("text", promptText);
        Map<String, Object> part2 = Map.of("inline_data", Map.of("mime_type", "image/jpeg", "data", base64));
        Map<String, Object> content = Map.of("parts", List.of(part1, part2));
        return new GeminiRequest(List.of(content));
    }

    /* ---------- Together ---------- */
    /**
     * Together API용 요청 생성
     */
    public TogetherRequest toTogetherReq(byte[] img, String pet) {
        String base64 = Base64.getEncoder().encodeToString(img);
        String promptText = detailedPrompt(pet, "");
        Map<String, Object> imageData = Map.of("format", "jpeg", "data", base64);
        Map<String, Object> message = Map.of(
                "role", "user",
                "content", List.of(
                        Map.of("type", "text", "text", promptText),
                        Map.of("type", "image_data", "image_data", imageData)
                )
        );
        return new TogetherRequest(prop.getLlamaModel(), List.of(message));
    }

    /* ---------- 내부 ---------- */
    /**
     * 상세 분석 보고서용 프롬프트 템플릿
     */
    private String detailedPrompt(String pet, String species) {
        return String.format(
                """
    반려동물 '%s'(종류: %s)에 대한 분석 보고서를 작성해줘.
    
    - 종류
    - 품종(믹스견은 추정 근거)
    - 외형(크기·털 색·특징)
    - 무게(1~40kg)
    - 맹수 여부
    - 감정·행동
    - 기타 특이사항(목줄·배경 등)
    
    보호자가 이해하기 쉬운 문장과 사용자 친화적으로 요약해줘.
    """,
                pet,
                species == null || species.isBlank() ? "알 수 없음" : species
        );
    }
}
