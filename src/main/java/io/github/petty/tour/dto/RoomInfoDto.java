package io.github.petty.tour.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter
@Setter
@NoArgsConstructor
public class RoomInfoDto {

    private Long contentId;
    private Integer contentTypeId;

    // RoomInfo 필드 (contentTypeId == 32)
    private String roomInfoNo;
    private String roomTitle;
    private String roomSize1;
    private Integer roomCount;
    private Integer roomBaseCount;
    private Integer roomMaxCount;
    private String roomOffSeasonMinFee1;
    private String roomOffSeasonMinFee2;
    private String roomPeakSeasonMinFee1;
    private String roomPeakSeasonMinFee2;
    private String roomIntro;
    private String roomBathFacility;
    private String roomBath;
    private String roomHomeTheater;
    private String roomAirCondition;
    private String roomTv;
    private String roomPc;
    private String roomCable;
    private String roomInternet;
    private String roomRefrigerator;
    private String roomToiletries;
    private String roomSofa;
    private String roomCook;
    private String roomTable;
    private String roomHairdryer;
    private String roomSize2; // 제곱미터
    private String roomImg1;
    private String roomImg1Alt;
    private String roomImg2;
    private String roomImg2Alt;
    private String roomImg3;
    private String roomImg3Alt;
    private String roomImg4;
    private String roomImg4Alt;
    private String roomImg5;
    private String roomImg5Alt;
}