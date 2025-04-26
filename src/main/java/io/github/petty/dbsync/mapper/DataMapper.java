package io.github.petty.dbsync.mapper;

import io.github.petty.dbsync.dto.*;
import io.github.petty.tour.entity.*;
import org.mapstruct.*;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper(
        componentModel = "spring",
        uses = {TypeConversionHelper.class},
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.SET_TO_NULL
)
public interface DataMapper {

    /**
     * 여러 API DTO를 취합하여 Content Entity 및 연관 Entity를 생성하고 관계를 설정합니다.
     */
    @Mappings({
            // Content 기본 정보 매핑 ()
            @Mapping(target = "contentId", source = "commonDto.contentId"),
            @Mapping(target = "contentTypeId", source = "commonDto.contentTypeId"),
            @Mapping(target = "title", source = "commonDto.title"),
            @Mapping(target = "addr1", source = "commonDto.addr1"),
            @Mapping(target = "addr2", source = "commonDto.addr2"),
            @Mapping(target = "areaCode", source = "commonDto.areaCode", qualifiedByName = "StringToInteger"),
            @Mapping(target = "sigunguCode", source = "commonDto.sigunguCode", qualifiedByName = "StringToInteger"),
            @Mapping(target = "cat1", source = "commonDto.cat1"),
            @Mapping(target = "cat2", source = "commonDto.cat2"),
            @Mapping(target = "cat3", source = "commonDto.cat3"),
            @Mapping(target = "firstImage", source = "commonDto.firstImage"),
            @Mapping(target = "firstImage2", source = "commonDto.firstImage2"),
            @Mapping(target = "cpyrhtDivCd", source = "commonDto.cpyrhtDivCd"),
            @Mapping(target = "createdTime", source = "commonDto.createdTime", qualifiedByName = "StringToInstant"),
            @Mapping(target = "modifiedTime", source = "commonDto.modifiedTime", qualifiedByName = "StringToInstant"),
            @Mapping(target = "mapX", source = "commonDto.mapX", qualifiedByName = "StringToBigDecimal"),
            @Mapping(target = "mapY", source = "commonDto.mapY", qualifiedByName = "StringToBigDecimal"),
            @Mapping(target = "mlevel", source = "commonDto.mlevel", qualifiedByName = "StringToInteger"),
            @Mapping(target = "tel", source = "commonDto.tel"),
            @Mapping(target = "telName", source = "commonDto.telName"),
            @Mapping(target = "homepage", source = "commonDto.homepage"),
            @Mapping(target = "overview", source = "commonDto.overview"),
            @Mapping(target = "zipcode", source = "commonDto.zipcode"),
            @Mapping(target = "location", source = "commonDto", qualifiedByName="dtoToPoint"),
            @Mapping(target = "contentIntro", source = "introDto"), // Map DetailIntroDto -> ContentIntro
            @Mapping(target = "petTourInfo", source = "petTourDto"), // Map DetailPetDto -> PetTourInfo
            @Mapping(target = "contentImages", source = "imageDtoList"), // Map List<DetailImageDto> -> Set<ContentImage>
            @Mapping(target = "contentInfos", ignore = true),
            @Mapping(target = "roomInfos", ignore = true)
    })
    Content mapToContentGraph(DetailCommonDto commonDto,
                            DetailIntroDto introDto, DetailPetDto petTourDto,
                            List<DetailImageDto> imageDtoList);

    // === Public Method to Create Content Entity ===
    default Content toContentEntity(DetailCommonDto commonDto,
                                    DetailIntroDto introDto, DetailPetDto petTourDto,
                                    List<DetailInfoDto> infoDtoList, List<DetailImageDto> imageDtoList) {

        if (commonDto == null) {
            return null;
        }

        // 1. Map the main graph using the internal method
        Content content = mapToContentGraph(commonDto, introDto, petTourDto, imageDtoList);
        // 2. Establish Bi-directional Relationships & Handle Conditional Lists
        if (content != null) {

            // Set parent reference in children for JPA cascade/management
            if (content.getContentIntro() != null) {
                content.getContentIntro().setContent(content);
                // ID is set by @MapsId during persistence, no need to set here
                // content.getContentIntro().setContentId(content.getContentId()); // NO!
            }
            if (content.getPetTourInfo() != null) {
                content.getPetTourInfo().setContent(content);
                // ID is set by @MapsId during persistence
                // content.getPetTourInfo().setContentId(content.getContentId()); // NO!
            }
            if (content.getContentImages() != null) {
                content.getContentImages().forEach(img -> img.setContent(content));
            }


            Integer contentTypeId = content.getContentTypeId();
            Set<ContentInfo> contentInfos = toContentInfoList(infoDtoList, contentTypeId);
            Set<RoomInfo> roomInfos = toRoomInfoList(infoDtoList, contentTypeId);

            contentInfos.forEach(info -> info.setContent(content)); // Set parent
            roomInfos.forEach(room -> room.setContent(content));   // Set parent

            content.setContentInfos(contentInfos);
            content.setRoomInfos(roomInfos);
        }

        return content;
    }

    @Mappings({
            // ID handled by @MapsId/JPA
            @Mapping(target = "contentId", ignore = true),
            // Parent relationship handled by cascade/setter
            @Mapping(target = "content", ignore = true),
            @Mapping(target = "introDetails", source = "dynamicFields")
    })
    ContentIntro mapIntroDtoToContentIntro(DetailIntroDto dto);



    @Mappings({
            // ID handled by @MapsId/JPA
            @Mapping(target = "contentId", ignore = true),
            // Parent relationship handled by cascade/setter
            @Mapping(target = "content", ignore = true)
    })
    PetTourInfo mapPetDtoToPetTourInfo(DetailPetDto dto);



    @Mappings({
            @Mapping(target = "id", ignore = true), // Auto Increment
            // Parent relationship handled by cascade/setter
            @Mapping(target = "content", ignore = true)
    })
    ContentImage mapImageDtoToContentImage(DetailImageDto dto);




    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "content", ignore = true), // Ignore parent
    })
    ContentInfo toContentInfo(DetailInfoDto dto);


    @Mappings({
            @Mapping(target = "id", ignore = true), // Auto Increment
            @Mapping(target = "roomOffSeasonMinFee1", source = "roomOffSeasonMinFee1"),
            @Mapping(target = "roomOffSeasonMinFee2", source = "roomOffSeasonMinFee2"),
            @Mapping(target = "roomPeakSeasonMinFee1", source = "roomPeakSeasonMinFee1"),
            @Mapping(target = "roomPeakSeasonMinFee2", source = "roomPeakSeasonMinFee2"),
            @Mapping(target = "roomBathFacility", source = "roomBathFacility", qualifiedByName = "StringToBooleanY"),
            @Mapping(target = "roomBath", source = "roomBath", qualifiedByName = "StringToBooleanY"),
            @Mapping(target = "roomHomeTheater", source = "roomHomeTheater", qualifiedByName = "StringToBooleanY"),
            @Mapping(target = "roomAirCondition", source = "roomAirCondition", qualifiedByName = "StringToBooleanY"),
            @Mapping(target = "roomTv", source = "roomTv", qualifiedByName = "StringToBooleanY"),
            @Mapping(target = "roomPc", source = "roomPc", qualifiedByName = "StringToBooleanY"),
            @Mapping(target = "roomCable", source = "roomCable", qualifiedByName = "StringToBooleanY"),
            @Mapping(target = "roomInternet", source = "roomInternet", qualifiedByName = "StringToBooleanY"),
            @Mapping(target = "roomRefrigerator", source = "roomRefrigerator", qualifiedByName = "StringToBooleanY"),
            @Mapping(target = "roomToiletries", source = "roomToiletries", qualifiedByName = "StringToBooleanY"),
            @Mapping(target = "roomSofa", source = "roomSofa", qualifiedByName = "StringToBooleanY"),
            @Mapping(target = "roomCook", source = "roomCook", qualifiedByName = "StringToBooleanY"),
            @Mapping(target = "roomTable", source = "roomTable", qualifiedByName = "StringToBooleanY"),
            @Mapping(target = "roomHairdryer", source = "roomHairdryer", qualifiedByName = "StringToBooleanY"),
            @Mapping(target = "content", ignore = true)

    })
    RoomInfo toRoomInfo(DetailInfoDto dto);


    default Set<ContentInfo> toContentInfoList(List<DetailInfoDto> dtoList, Integer contentTypeId) {
        if (contentTypeId == null || contentTypeId == 32 || dtoList == null || dtoList.isEmpty()) { // 32(숙박) 제외
            return new LinkedHashSet<>(); // Return mutable empty set
        }
        return dtoList.stream()
                .map(this::toContentInfo)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    default Set<RoomInfo> toRoomInfoList(List<DetailInfoDto> dtoList, Integer contentTypeId) {
        if (contentTypeId == null || contentTypeId != 32 || dtoList == null || dtoList.isEmpty()) { // 32(숙박)만 해당
            return new LinkedHashSet<>(); // Return mutable empty set
        }
        return dtoList.stream()
                .map(this::toRoomInfo)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }



}