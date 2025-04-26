package io.github.petty.dbsync.dto;

// --- Item DTO 정의 ---

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

// 2. detailIntro 용 Item DTO
@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class DetailIntroDto {
    @JsonProperty("contentid")
    private Long contentId;

    @JsonProperty("contenttypeid")
    private Integer contentTypeId;

    // contentid, contenttypeid 외의 모든 다른 필드를 이 Map에 저장
    private Map<String, Object> dynamicFields = new HashMap<>();

    @JsonAnySetter // JSON의 알려지지 않은 필드를 이 메소드를 통해 Map에 추가
    public void addDynamicField(String fieldName, Object value) {
        dynamicFields.put(fieldName, value);
    }

// 필요 시 dynamicFields에 대한 Getter (@Data에 포함되지만 명시적으로 보여줌)
    // public Map<String, Object> getDynamicFields() {
    //     return dynamicFields;
    // }
}

