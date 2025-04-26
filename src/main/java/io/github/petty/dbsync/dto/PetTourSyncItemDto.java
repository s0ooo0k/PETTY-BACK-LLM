package io.github.petty.dbsync.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

// 6. petTourSyncList 용 Item DTO
@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class PetTourSyncItemDto {

    @JsonProperty("contentid")
    private Long contentId;
    @JsonProperty("contenttypeid")
    private Integer contentTypeId;
    private String title;
    private String addr1;
    private String addr2;
    @JsonProperty("areacode")
    private String areaCode;
    @JsonProperty("sigungucode")
    private String sigunguCode;
    private String cat1;
    private String cat2;
    private String cat3;
    @JsonProperty("createdtime")
    private String createdTime; // YYYYMMDDHHMMSS 형식 String
    @JsonProperty("modifiedtime")
    private String modifiedTime; // YYYYMMDDHHMMSS 형식 String
    @JsonProperty("firstimage")
    private String firstImage;
    @JsonProperty("firstimage2")
    private String firstImage2;
    private String cpyrhtDivCd;
    @JsonProperty("mapx")
    private String mapX; // Decimal 형식 String
    @JsonProperty("mapy")
    private String mapY; // Decimal 형식 String
    @JsonProperty("mlevel")
    private String mLevel;
    private String tel;
    @JsonProperty("zipcode")
    private String zipCode;
    @JsonProperty("showflag")
    private String showFlag; // "1" or null
}
