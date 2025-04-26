package io.github.petty.dbsync.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

// 최상위 응답 구조
@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ApiResponseDto<T> { // T는 Body DTO 타입
    @JsonProperty("response")
    private ResponseDto<T> response;
}

