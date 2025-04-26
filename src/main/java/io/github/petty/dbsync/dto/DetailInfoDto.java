package io.github.petty.dbsync.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

// 3. detailInfo 용 Item DTO (RoomInfo + ContentInfo 필드 통합)
// 실제로는 contentTypeId에 따라 사용하는 필드가 다름.
// 파싱 편의를 위해 우선 모든 필드를 포함하고, 서비스 로직에서 구분하여 사용.
@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class DetailInfoDto {

    @JsonProperty("contentid")
    private Long contentId;

    @JsonProperty("contenttypeid")
    private Integer contentTypeId;

    // ContentInfo 필드 (contentTypeId != 32)
    @JsonProperty("fldgubun")
    private String fldGubun;

    @JsonProperty("infoname")
    private String infoName;

    @JsonProperty("infotext")
    private String infoText;

    @JsonProperty("serialnum")
    private String serialNum; // ContentInfo의 serialnum

    // RoomInfo 필드 (contentTypeId == 32)
    @JsonProperty("roominfono")
    private String roomInfoNo; // RoomInfo의 serialnum 역할? 확인 필요

    @JsonProperty("roomtitle")
    private String roomTitle;

    @JsonProperty("roomsize1")
    private String roomSize1; // 평

    @JsonProperty("roomcount")
    private Integer roomCount;

    @JsonProperty("roombasecount")
    private Integer roomBaseCount;

    @JsonProperty("roommaxcount")
    private Integer roomMaxCount;

    @JsonProperty("roomoffseasonminfee1")
    private String roomOffSeasonMinFee1; // Decimal? or String? API 확인 필요

    @JsonProperty("roomoffseasonminfee2")
    private String roomOffSeasonMinFee2;

    @JsonProperty("roompeakseasonminfee1")
    private String roomPeakSeasonMinFee1;

    @JsonProperty("roompeakseasonminfee2")
    private String roomPeakSeasonMinFee2;

    @JsonProperty("roomintro")
    private String roomIntro; // Text

    @JsonProperty("roombathfacility")
    private String roomBathFacility; // tinyint(1) -> "1"/"0" or "Y"/"N"? API 확인 필요

    @JsonProperty("roombath")
    private String roomBath;

    @JsonProperty("roomhometheater")
    private String roomHomeTheater;

    @JsonProperty("roomaircondition")
    private String roomAirCondition;

    @JsonProperty("roomtv")
    private String roomTv;

    @JsonProperty("roompc")
    private String roomPc;

    @JsonProperty("roomcable")
    private String roomCable;

    @JsonProperty("roominternet")
    private String roomInternet;

    @JsonProperty("roomrefrigerator")
    private String roomRefrigerator;

    @JsonProperty("roomtoiletries")
    private String roomToiletries;

    @JsonProperty("roomsofa")
    private String roomSofa;

    @JsonProperty("roomcook")
    private String roomCook;

    @JsonProperty("roomtable")
    private String roomTable;

    @JsonProperty("roomhairdryer")
    private String roomHairdryer;

    @JsonProperty("roomsize2")
    private String roomSize2; // 제곱미터

    @JsonProperty("roomimg1")
    private String roomImg1;

    @JsonProperty("roomimg1alt")
    private String roomImg1Alt;

    @JsonProperty("roomimg2")
    private String roomImg2;

    @JsonProperty("roomimg2alt")
    private String roomImg2Alt;

    @JsonProperty("roomimg3")
    private String roomImg3;

    @JsonProperty("roomimg3alt")
    private String roomImg3Alt;

    @JsonProperty("roomimg4")
    private String roomImg4;

    @JsonProperty("roomimg4alt")
    private String roomImg4Alt;

    @JsonProperty("roomimg5")
    private String roomImg5;

    @JsonProperty("roomimg5alt")
    private String roomImg5Alt;
}