package io.github.petty.tour.mapper;


import io.github.petty.tour.dto.*;
import io.github.petty.tour.entity.*;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.math.BigDecimal;
import java.util.List;

// MapStruct Mapper 설정: Spring 컴포넌트로 만들고, 필요한 매퍼를 주입받을 수 있도록 설정
@Mapper(componentModel = "spring")
public interface ContentMapper {

// --- Content -> DetailCommonDto ---

    @Mapping(target = "images", ignore = true)
    @Mapping(target = "infos", ignore = true)
    @Mapping(target = "rooms", ignore = true)
    @Mapping(target = "introDetails", ignore = true)
    DetailCommonDto contentToDetailCommonDto(Content content);


    // --- ContentImage -> DetailImageDto ---
    @Mapping(target = "contentId", ignore = true)
    DetailImageDto contentImageToDetailImageDto(ContentImage image);

    // --- ContentInfo -> DetailInfoDto ---
    @Mapping(target = "contentId", ignore = true)
    @Mapping(target = "contentTypeId", ignore = true)
    DetailInfoDto contentInfoToDetailInfoDto(ContentInfo info);

    // --- ContentIntro -> DetailIntroDto ---
    DetailIntroDto contentIntroToDetailIntroDto(ContentIntro intro);

    // --- PetTourInfo -> DetailPetDto ---
    DetailPetDto petTourInfoToDetailPetDto(PetTourInfo petInfo);

    // --- RoomInfo -> RoomInfoDto ---
    @Mapping(target = "contentId", ignore = true)
    @Mapping(target = "contentTypeId", ignore = true)
    @Mapping(source = "roomOffSeasonMinFee1", target = "roomOffSeasonMinFee1", qualifiedByName = "bigDecimalToString")
    @Mapping(source = "roomOffSeasonMinFee2", target = "roomOffSeasonMinFee2", qualifiedByName = "bigDecimalToString")
    @Mapping(source = "roomPeakSeasonMinFee1", target = "roomPeakSeasonMinFee1", qualifiedByName = "bigDecimalToString")
    @Mapping(source = "roomPeakSeasonMinFee2", target = "roomPeakSeasonMinFee2", qualifiedByName = "bigDecimalToString")
    @Mapping(source = "roomBathFacility", target = "roomBathFacility", qualifiedByName = "booleanToYN")
    @Mapping(source = "roomBath", target = "roomBath", qualifiedByName = "booleanToYN")
    @Mapping(source = "roomHomeTheater", target = "roomHomeTheater", qualifiedByName = "booleanToYN")
    @Mapping(source = "roomAirCondition", target = "roomAirCondition", qualifiedByName = "booleanToYN")
    @Mapping(source = "roomTv", target = "roomTv", qualifiedByName = "booleanToYN")
    @Mapping(source = "roomPc", target = "roomPc", qualifiedByName = "booleanToYN")
    @Mapping(source = "roomCable", target = "roomCable", qualifiedByName = "booleanToYN")
    @Mapping(source = "roomInternet", target = "roomInternet", qualifiedByName = "booleanToYN")
    @Mapping(source = "roomRefrigerator", target = "roomRefrigerator", qualifiedByName = "booleanToYN")
    @Mapping(source = "roomToiletries", target = "roomToiletries", qualifiedByName = "booleanToYN")
    @Mapping(source = "roomSofa", target = "roomSofa", qualifiedByName = "booleanToYN")
    @Mapping(source = "roomCook", target = "roomCook", qualifiedByName = "booleanToYN")
    @Mapping(source = "roomTable", target = "roomTable", qualifiedByName = "booleanToYN")
    @Mapping(source = "roomHairdryer", target = "roomHairdryer", qualifiedByName = "booleanToYN")
    RoomInfoDto roomInfoToRoomInfoDto(RoomInfo room);

    // --- Projection -> TourSummaryDto 매핑 메소드 ---
    @Mapping(source = "distance", target = "distanceMeters")
    TourSummaryDto projectionToTourSummaryDto(TourSummaryProjection projection);


    // Area -> CodeNameDto 매핑
    @Mapping(source = "areaCode", target = "code") // 필드 이름이 다를 경우 명시적 매핑
    @Mapping(source = "areaName", target = "name")
    CodeNameDto areaToCodeNameDto(Area area);
    List<CodeNameDto> areasToCodeNameDtos(List<Area> areas);

    // Sigungu -> CodeNameDto 매핑
    @Mapping(source = "sigunguCode", target = "code") // 필드 이름이 다를 경우 명시적 매핑
    @Mapping(source = "sigunguName", target = "name")
    CodeNameDto sigunguToCodeNameDto(Sigungu sigungu);
    List<CodeNameDto> sigungusToCodeNameDtos(List<Sigungu> sigungus);

    // --- Custom Mapping Helper Methods ---

    @Named("bigDecimalToString")
    default String bigDecimalToString(BigDecimal value) {
        return (value != null) ? value.stripTrailingZeros().toPlainString() : null;
        // stripTrailingZeros() 는 10.00 -> 10 으로 변환
        // toPlainString() 은 지수 표현(e.g., 1E+1) 대신 일반 숫자 문자열로 변환
    }

    @Named("booleanToYN")
    default String booleanToYN(Boolean value) {
        if (value == null) {
            return null; // 또는 "N" 또는 빈 문자열 등 요구사항에 맞게 처리
        }
        return value ? "Y" : "N"; // 또는 "1" : "0"
    }

    // Set<Entity> -> List<DTO> 변환이 자동으로 안 될 경우 명시적으로 정의 가능
    // (하지만 보통 MapStruct가 단일 매핑 메소드를 보고 자동으로 처리해줌)
    /*
    default List<DetailImageDto> contentImagesToDetailImageDtos(Set<ContentImage> images) {
        if (images == null) {
            return null;
        }
        return images.stream()
                     .map(this::contentImageToDetailImageDto) // this:: 사용 주의 (default 메소드 내)
                     .collect(Collectors.toList());
    }
    // 다른 Set -> List 변환도 유사하게 정의 가능
    */
}