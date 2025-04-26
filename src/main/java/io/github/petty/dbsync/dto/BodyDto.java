package io.github.petty.dbsync.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

// body 필드 구조 (페이징 정보 포함)
@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class BodyDto<I> { // I는 Items DTO 타입
    @JsonProperty("items")
    private I items; // 단일 객체 또는 리스트를 담는 ItemsDto

    @JsonProperty("numOfRows")
    private Integer numOfRows;

    @JsonProperty("pageNo")
    private Integer pageNo;

    @JsonProperty("totalCount")
    private Integer totalCount;
}
