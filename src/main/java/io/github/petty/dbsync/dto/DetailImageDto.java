package io.github.petty.dbsync.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

// 4. detailImage 용 Item DTO
@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class DetailImageDto {

    @JsonProperty("contentid")
    private Long contentId;

    @JsonProperty("imgname")
    private String imgName;

    @JsonProperty("originimgurl")
    private String originImgUrl;

    @JsonProperty("serialnum")
    private String serialNum;

    @JsonProperty("smallimageurl")
    private String smallImageUrl;

    private String cpyrhtDivCd;
}