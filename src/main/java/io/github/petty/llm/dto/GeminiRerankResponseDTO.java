package io.github.petty.llm.dto;

import java.util.List;

public record GeminiRerankResponseDTO(
        List<RerankedPlace> rankedPlaces
) {
    public record RerankedPlace(
            String contentId,
            String recommendReason
    ) {}
}
