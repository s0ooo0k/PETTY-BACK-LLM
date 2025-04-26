package io.github.petty.tour.dto;


/**
 * findByLocationNative 네이티브 쿼리의 결과를 담기 위한 프로젝션 인터페이스.
 * 각 getter 메소드는 SELECT 절의 컬럼 또는 별칭과 매칭됩니다.
 */
public interface TourSummaryProjection {
    Long getContentId();
    String getTitle();
    String getAddr1();
    Integer getContentTypeId();
    String getFirstImage(); // DB의 firstimage 컬럼 값이지만, 쿼리에서 'firstImage' 별칭 사용
    String getCat1();
    Double getDistance();   // 계산된 거리 값 (미터 단위)
}