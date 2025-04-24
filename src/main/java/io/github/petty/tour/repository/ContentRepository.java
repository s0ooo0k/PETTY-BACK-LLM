package io.github.petty.tour.repository;


import io.github.petty.tour.dto.TourSummaryProjection;
import io.github.petty.tour.entity.Content;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface ContentRepository extends JpaRepository<Content, Long> {

    List<Content> findByContentId(Long contentId);
    @Query("""
        SELECT c FROM Content c
        LEFT JOIN FETCH c.petTourInfo
    """)

    /**
     * 주어진 contentId 목록에 해당하는 Content 및 연관된 모든 데이터(Cascade)를 삭제합니다.
     * @param contentIds 삭제할 contentId 리스트
     * @return 삭제된 레코드 수 (반환 타입은 void 또는 long 등 조정 가능)
     */
    long deleteAllByContentIdIn(List<Long> contentIds);

    // 테스트용 20개 추출
    List<Content> findTop20ByOrderByContentIdAsc();

    // 1. 지역 기반 검색 (JPQL 사용 예시 - Optional 파라미터 처리)
    @Query(
            value = "SELECT c.contentId, c.title, c.addr1, c.contentTypeId, c.firstImage, c.cat1 FROM content c WHERE c.areaCode = :areaCode " +
                    "AND (:sigunguCode IS NULL OR c.sigunguCode = :sigunguCode) " +
                    "AND (:contentTypeId IS NULL OR c.contentTypeId = :contentTypeId)",
            nativeQuery = true)
    Page<TourSummaryProjection> findByAreaCriteria(@Param("areaCode") Integer areaCode,
                                                   @Param("sigunguCode") Integer sigunguCode,
                                                   @Param("contentTypeId") Integer contentTypeId,
                                                   Pageable pageable);

    @Query(
            value =
                    "SELECT c.contentid, c.title, c.addr1, c.contentTypeId, c.firstImage, c.cat1, " +
                            "ST_Distance_Sphere(c.location, ST_SRID(POINT(:lon, :lat), 4326)) as distance " +
                            "FROM content c " +
                            "WHERE ST_Distance_Sphere(c.location, ST_SRID(POINT(:lon, :lat), 4326)) <= :radius " +
                            "AND (:contentTypeId IS NULL OR c.contenttypeid = :contentTypeId) " +
                            "ORDER BY distance ASC", // Order by distance
            countQuery = "SELECT count(*) FROM content c " +
                    "WHERE ST_Distance_Sphere(c.location, ST_SRID(POINT(:lon, :lat), 4326)) <= :radius " +
                    "AND (:contentTypeId IS NULL OR c.contenttypeid = :contentTypeId)",
            nativeQuery = true)
    Page<TourSummaryProjection> findByLocationNative( // Return Object[] or a dedicated projection interface
                                                      @Param("lon") BigDecimal lon,
                                                      @Param("lat") BigDecimal lat,
                                                      @Param("radius") int radius,
                                                      @Param("contentTypeId") Integer contentTypeId,
                                                      Pageable pageable);

    @Query("SELECT c FROM Content c LEFT JOIN FETCH c.petTourInfo WHERE c.contentId = :contentId")
    Optional<Content> findByIdWithPetInfo(@Param("contentId") Long contentId);
}
