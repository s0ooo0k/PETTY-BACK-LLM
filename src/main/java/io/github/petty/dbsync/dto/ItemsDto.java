package io.github.petty.dbsync.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

// items 필드 구조 (실제 데이터 item 포함)
@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ItemsDto<T> { // ItemType은 실제 데이터 DTO 또는 List<DTO>
    @JsonProperty("item")
    private List<T> item;
}
