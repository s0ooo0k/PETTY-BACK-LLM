package io.github.petty.tour.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class DetailCommonDto {
    private Long contentId;
    private Integer contentTypeId;
    private String title;
    private String addr1;
    private String addr2;
    private Integer areaCode;
    private Integer sigunguCode;
    private String cat1;
    private String cat2;
    private String cat3;
    private Instant createdTime;
    private Instant modifiedTime;
    private String firstImage;
    private String firstImage2;
    private String cpyrhtDivCd;
    private BigDecimal mapX;
    private BigDecimal mapY;
    private Integer mlevel;
    private String tel;
    private String telName;
    private String homepage;
    private String overview;
    private String zipcode;

    // Related data (using nested DTOs is recommended)
    private List<DetailImageDto> images;
    private List<DetailInfoDto> infos;
    private List<RoomInfoDto> rooms;
    private DetailIntroDto introDetails;
    private DetailPetDto petTourInfo;
}