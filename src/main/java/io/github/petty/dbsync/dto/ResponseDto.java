package io.github.petty.dbsync.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

// response 필드 내부 구조
@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ResponseDto<T> { // T는 Body DTO 타입
    @JsonProperty("header")
    private HeaderDto header;

    @JsonProperty("body")
    private T body; // API 종류에 따라 Body 타입이 달라짐
}
