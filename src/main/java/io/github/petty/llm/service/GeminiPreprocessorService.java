package io.github.petty.llm.service;

import io.github.petty.tour.entity.Content;

public interface GeminiPreprocessorService {
    /**
     * Content(장소 정보)로부터 전처리된 텍스트를 생성합니다.
     * @param content 전처리할 Content 엔티티
     * @return Gemini를 활용한 전처리 텍스트 결과
     */
    String preprocessContent(Content content);
}
