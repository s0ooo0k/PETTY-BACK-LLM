package io.github.petty.tour.service;

import io.github.petty.tour.dto.*; // Import all required DTOs
import io.github.petty.tour.entity.*; // Import required Entities
import io.github.petty.tour.exception.ResourceNotFoundException; // Define this custom exception
import io.github.petty.tour.mapper.ContentMapper; // RECOMMENDED: Define a MapStruct mapper
import io.github.petty.tour.dto.TourSummaryProjection;
import io.github.petty.tour.repository.*; // Import required Repositories
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // Important for JPA operations

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor // Lombok for constructor injection
public class TourServiceImpl implements TourService {

    private final AreaRepository areaRepository;
    private final SigunguRepository sigunguRepository;
    private final ContentRepository contentRepository;
    private final ContentMapper contentMapper;



    @Transactional(readOnly = true)
    public List<CodeNameDto> getAreas(Integer areaCode) {
        if (areaCode == null) {
            log.debug("Fetching all area codes using Mapper");
            return contentMapper.areasToCodeNameDtos(areaRepository.findAll());
        }
        log.debug("Fetching sigungu codes for areaCode: {} using Mapper", areaCode);

        return contentMapper.sigungusToCodeNameDtos(sigunguRepository.findByAreaCode(areaCode));
    }



    @Override
    @Transactional(readOnly = true)
    public DetailCommonDto getContentDetailById(Long contentId) {
        log.debug("Fetching content detail for ID: {}", contentId);

        Content content = contentRepository.findByIdWithPetInfo(contentId)
                .orElseThrow(() -> new ResourceNotFoundException("Content not found with id: " + contentId));


        // 매퍼를 사용하여 엔티티 (및 관련 엔티티)를 DTO로 변환합니다.
        // @Transactional은 매핑 중에 관련 게으른 컬렉션을 로드 할 수 있도록합니다.
        DetailCommonDto dto = contentMapper.contentToDetailCommonDto(content);


        log.info("Successfully fetched content detail for ID: {}", contentId);
        return dto;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TourSummaryDto> searchByArea(Integer areaCode, Integer sigunguCode, Integer contentTypeId, Pageable pageable) {
        log.debug("지역별 검색: areaCode={}, sigunguCode={}, contentTypeId={}, pageable={}",
                areaCode, sigunguCode, contentTypeId, pageable);

        // 이와 같은 저장소 메소드가 존재한다고 가정합니다 (@Query 또는 Criteria API 사용)
        Page<TourSummaryProjection> contentPage = contentRepository.findByAreaCriteria(areaCode, sigunguCode, contentTypeId, pageable);
        contentPage.getContent().forEach(content -> log.info("content: {}", content.getContentId()));
        // Map Page<Content> to Page<ContentSummaryDto>
        return contentPage.map(contentMapper::projectionToTourSummaryDto); // Using MapStruct
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TourSummaryDto> searchByLocation(BigDecimal mapX, BigDecimal mapY, Integer radius, Integer contentTypeId, Pageable pageable) {
        log.debug("Searching by location: mapX={}, mapY={}, radius={}, contentTypeId={}, pageable={}",
                mapX, mapY, radius, contentTypeId, pageable);

        Page<TourSummaryProjection> projectionPage = contentRepository.findByLocationNative(mapX, mapY, radius, contentTypeId, pageable);

        projectionPage.getContent().forEach(content -> log.info("content: {}", content.getContentId()));

        return projectionPage.map(contentMapper::projectionToTourSummaryDto);
    }


}