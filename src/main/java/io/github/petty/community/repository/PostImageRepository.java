package io.github.petty.community.repository;

import io.github.petty.community.entity.PostImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface PostImageRepository extends JpaRepository<PostImage, Long> {

    // ✅ 게시글 ID로 모든 이미지 조회 (리스트/상세 출력 시 필요)
    List<PostImage> findByPostIdOrderByOrderingAsc(Long postId);

    // ✅ 게시글 ID로 한 번에 삭제하고 싶을 때 (예: 게시글 삭제 시)
    @Modifying
    @Transactional
    @Query("DELETE FROM PostImage p WHERE p.post.id = :postId")
    void deleteByPostId(Long postId);
}
