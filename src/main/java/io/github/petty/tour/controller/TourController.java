package io.github.petty.tour.controller;


import io.github.petty.tour.dto.CodeNameDto;
import io.github.petty.tour.dto.DetailCommonDto;
import io.github.petty.tour.dto.TourSummaryDto;
import io.github.petty.tour.service.TourService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/contents")
@RequiredArgsConstructor
public class TourController {

    private final TourService tourService;

    /**
     * Endpoint to get all Area codes, returned as CodeNameDto list.
     * GET /api/v1/codes/areas
     * @return ResponseEntity containing a list of CodeNameDto (Areas).
     */
    @GetMapping("/codes")
    public ResponseEntity<List<CodeNameDto>> getAreaCodes(@RequestParam(name = "areaCode", required = false) Integer areaCode) {
        if (areaCode == null) {
            log.info("Request received for all area codes");
        }else {
            log.info("Request received for sigungu codes with areaCode: {}", areaCode);
        }
        List<CodeNameDto> areas = tourService.getAreas(areaCode);

        return ResponseEntity.ok(areas);
    }



    /**
     * 특정 콘텐츠의 상세 정보를 조회합니다.
     * GET /api/v1/contents/{contentId}
     * @param contentId 조회할 콘텐츠 ID
     * @return ResponseEntity<DetailCommonDto>
     */
    @GetMapping("/{contentId}")
    public ResponseEntity<DetailCommonDto> getContentById(@PathVariable Long contentId) {
        log.info("Request received for content detail: contentId={}", contentId);
        DetailCommonDto contentDetail = tourService.getContentDetailById(contentId);
        return ResponseEntity.ok(contentDetail);
    }

    /**
     * 지역 기반으로 콘텐츠 목록을 검색합니다 (페이징 포함).
     * GET /api/v1/contents/search/area?areaCode=...&sigunguCode=...&contentTypeId=...&page=...&size=...&sort=...
     *      *@param areaCode      지역 코드 (필수)
     * @param sigunguCode   시군구 코드 (선택)
     * @param contentTypeId 콘텐츠 타입 ID (선택)
     * @param pageable      페이징 정보 (기본값: size 10, 생성일 내림차순 정렬)
     * @return ResponseEntity<Page<ContentSummaryDto>>
     */
    @GetMapping("/search/area")
    public ResponseEntity<Page<TourSummaryDto>> searchByArea(
            @RequestParam Integer areaCode,
            @RequestParam(name = "sigunguCode", required = false) Integer sigunguCode,
            @RequestParam(required = false) Integer contentTypeId,
            @PageableDefault(size = 10, sort = "modifiedTime", direction = Sort.Direction.DESC) Pageable pageable) {

        log.info("Request received for area search: areaCode={}, sigunguCode={}, contentTypeId={}, pageable={}",
                areaCode, sigunguCode, contentTypeId, pageable);
        Page<TourSummaryDto> results = tourService.searchByArea(areaCode, sigunguCode, contentTypeId, pageable);
        return ResponseEntity.ok(results);
    }


    /**
     * 위치 기반으로 주변 콘텐츠 목록을 검색합니다 (페이징, 거리순 정렬 기본).
     * GET /api/v1/contents/search/location?mapX=...&mapY=...&radius=...&contentTypeId=...&page=...&size=...
     * @param mapX          중심점 경도 (longitude) (필수)
     * @param mapY          중심점 위도 (latitude) (필수)
     * @param radius        검색 반경 (미터 단위) (필수, e.g., 5000 for 5km)
     * @param contentTypeId 콘텐츠 타입 ID (선택)
     * @param pageable      페이징 정보 (page, size - sort는 거리순으로 고정될 수 있음)
     * @return ContentSummaryDto 페이지 (거리 정보 포함 가능) 포함 ResponseEntity
     */
    @GetMapping("/search/location")
    public ResponseEntity<Page<TourSummaryDto>> searchByLocation(
            @RequestParam BigDecimal mapX,
            @RequestParam BigDecimal mapY,
            @RequestParam Integer radius,
            @RequestParam(required = false) Integer contentTypeId,
            @PageableDefault(size = 10) Pageable pageable) {

        log.info("Request received for location search: mapX={}, mapY={}, radius={}, contentTypeId={}, pageable={}",
                mapX, mapY, radius, contentTypeId, pageable);

        Page<TourSummaryDto> results = tourService.searchByLocation(mapX, mapY, radius, contentTypeId, pageable);
        return ResponseEntity.ok(results);
    }

}