package io.github.petty.llm.dto;

import java.util.List;

public record GeminiRerankRequestDTO(
        String userPrompt,
        List<RecommendPlace> place
) {
    public record RecommendPlace(
            String contentId,
            String title,
            String addr,
            String description,
            String acmpyTypeCd,
            String acmpyPsblCpam,
            String acmpyNeedMtr
    ) {}
}


