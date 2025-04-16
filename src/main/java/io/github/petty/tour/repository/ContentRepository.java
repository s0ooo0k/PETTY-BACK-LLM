package io.github.petty.tour.repository;


import io.github.petty.tour.entity.Content;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
// Content (중심 테이블)
public interface ContentRepository extends JpaRepository<Content, Long> {
//    @Query("""
//        SELECT c FROM Content c
//        LEFT JOIN FETCH c.petTourInfo
//        LEFT JOIN FETCH c.contentIntro
//    """)
    @Query("""
        SELECT c FROM Content c
        LEFT JOIN FETCH c.petTourInfo
    """)
    List<Content> findAllWithDetails();
}