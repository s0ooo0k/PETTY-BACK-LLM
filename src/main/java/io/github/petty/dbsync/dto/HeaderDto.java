package io.github.petty.dbsync.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

// header 필드 구조
@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class HeaderDto {
    @JsonProperty("resultCode")
    private String resultCode; // "0000" 이면 정상

    @JsonProperty("resultMsg")
    private String resultMsg;  // "OK" 이면 정상
}
