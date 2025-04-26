package io.github.petty.tour.service;

import io.github.petty.tour.dto.CodeNameDto;
import io.github.petty.tour.dto.DetailCommonDto; // Assuming this is the final name
import io.github.petty.tour.dto.TourSummaryDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;

public interface TourService {


    /**
     * 모든 시도(Area) 정보를 CodeNameDto 리스트로 조회 (이름순 정렬)
     * @return List of CodeNameDto representing Areas.
     */
    public List<CodeNameDto> getAreas(Integer areaCode);


    /**
     * 콘텐츠 ID로 상세 정보를 조회합니다.
     * @param contentId 조회할 콘텐츠 ID
     * @return DetailCommonDto
     * @throws ResourceNotFoundException // Define this custom exception
     */
    DetailCommonDto getContentDetailById(Long contentId);

    /**
     * 지역 코드를 기반으로 콘텐츠 목록을 검색합니다.
     * @param areaCode 지역 코드 (필수)
     * @param sigunguCode 시군구 코드 (선택)
     * @param contentTypeId 콘텐츠 타입 ID (선택)
     * @param pageable 페이징 정보
     * @return Page<ContentSummaryDto>
     */
    Page<TourSummaryDto> searchByArea(Integer areaCode, Integer sigunguCode, Integer contentTypeId, Pageable pageable);

    /**
     * 위치(좌표 및 반경)를 기반으로 콘텐츠 목록을 검색합니다.
     * @param mapX 경도 (longitude)
     * @param mapY 위도 (latitude)
     * @param radius 반경 (미터)
     * @param contentTypeId 콘텐츠 타입 ID (선택)
     * @param pageable 페이징 정보 (결과는 거리순으로 정렬될 수 있음)
     * @return Page<ContentSummaryDto> (거리 정보 포함될 수 있음)
     */
    Page<TourSummaryDto> searchByLocation(BigDecimal mapX, BigDecimal mapY, Integer radius, Integer contentTypeId, Pageable pageable);



}
