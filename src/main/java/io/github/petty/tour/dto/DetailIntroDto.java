package io.github.petty.tour.dto;

// --- Item DTO 정의 ---

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

// 2. detailIntro 용 Item DTO
@Getter
@Setter
@NoArgsConstructor
public class DetailIntroDto {
    private Map<String, Object> introDetails = new HashMap<>();
}

