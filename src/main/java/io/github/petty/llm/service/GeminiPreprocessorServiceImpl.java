package io.github.petty.llm.service;

import com.fasterxml.jackson.databind.ObjectMapper;

import groovy.util.logging.Log4j;
import groovy.util.logging.Slf4j;
import io.github.petty.llm.common.ContentType;
import io.github.petty.llm.dto.GeminiRequestDTO;
import io.github.petty.llm.dto.GeminiResponseDTO;
import io.github.petty.tour.entity.Content;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;


@lombok.extern.slf4j.Slf4j
@Slf4j
@Service
@RequiredArgsConstructor
public class GeminiPreprocessorServiceImpl implements GeminiPreprocessorService {
    @Value("${gemini.api.key}")
    private String apiKey;

    private final ObjectMapper objectMapper;

    @Override
    public String preprocessContent(Content content) {
        try {
            String overview = content.getOverview();
            if (overview == null || overview.isBlank()) {
                log.warn("[전처리] Overview 없음, 기본 텍스트 사용 (contentId: {})", content.getContentId());
                return defaultPrepareText(content);
            }

            String finalPrompt = buildPrompt(overview);

            HttpClient httpClient = HttpClient.newHttpClient();
            String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent?key=%s"
                    .formatted(apiKey);
            log.info("[Gemini 요청] 프롬프트:\n{}", finalPrompt);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(
                            objectMapper.writeValueAsString(
                                    new GeminiRequestDTO(List.of(
                                            new GeminiRequestDTO.Content(List.of(
                                                    new GeminiRequestDTO.Part(finalPrompt)
                                            ))
                                    ))
                            )
                    ))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            GeminiResponseDTO resp = objectMapper.readValue(response.body(), GeminiResponseDTO.class);
            String geminiResponseText = resp.candidates().get(0).content().parts().get(0).text();
            return generateFinalText(content, geminiResponseText);
        } catch (Exception e) {
            return defaultPrepareText(content);
        }
    }

    private String defaultPrepareText(Content content) {
        String ContentTypeName = ContentType.fromCode(content.getContentTypeId()).getName();
        String defaultText = "%s은/는 %s 종류의 장소입니다.\n"
                .formatted(
                        content.getTitle(), ContentTypeName
                );
        return defaultText;
    }

    private String buildPrompt(String overview) {
        String prompt = """
                당신은 관광지 요약 전문 모델입니다.
                
                  아래 '장소 설명'을 읽고, 이 장소를 사람들이 관광 목적으로 이해하기 쉽게 자연스럽게 한글 문장으로 요약하세요.
                
                  - 설명이 충분하다면 주요 특징을 1~2문장 정도로 요약해주세요.
                  - 설명이 부실하거나 정보가 적으면 가능한 범위 내에서 간단히 요약하세요.
                  - 관광지 방문 목적, 활동 목적, 추천 대상을 포함하여 1문장으로 작성해주세요. 없다면 해당 설명에 맞는 문장을 생성해주세요.
                  - 결과는 포맷 없이 단순한 한글 문장으로 작성하세요.
                  - "키워드", "활동 목적", "추천 대상" 등의 제목이나 구분 없이 하나의 자연스러운 문단으로 작성하세요.
                  - 추가적인 주의사항, 요청사항, 키워드 리스트 등은 포함하지 마세요.
                  - 답변은 오직 자연스러운 한글 문장만 포함하세요.
                
                  [장소 설명]
                  %s
                """.formatted(overview);
        return prompt;
    }

    private String generateFinalText(Content content, String geminiResponse) {
        String ContentTypeName = ContentType.fromCode(content.getContentTypeId()).getName();
        String prompt = "%s은/는 %s 종류의 장소입니다.\n %s"
                .formatted(
                        content.getTitle(), ContentTypeName, geminiResponse
                );
        log.info(prompt);
        return prompt;
    }
}
