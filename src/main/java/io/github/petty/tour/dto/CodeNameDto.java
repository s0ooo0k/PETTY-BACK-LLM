package io.github.petty.tour.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 지역(Area) 또는 시군구(Sigungu)의 코드와 이름을 담는 공통 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CodeNameDto {
    private Integer code;
    private String name;
}