package io.github.petty.llm.service;

import java.util.Map;

public interface RecommendService {
    String recommend(Map<String, String> promptMap);
}
