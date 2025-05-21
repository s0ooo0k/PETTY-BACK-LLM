package io.github.petty.llm.service;

import io.github.petty.llm.dto.RecommendResponseDTO;

import java.util.Map;

public interface RecommendService {
    RecommendResponseDTO recommend(Map<String, String> promptMap);
}
