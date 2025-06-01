package io.github.petty.community.repository;

import io.github.petty.community.entity.Post;
import io.github.petty.community.entity.PostLike;
import io.github.petty.users.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

public interface PostLikeRepository extends JpaRepository<PostLike, Long> {

    // ✅ 사용자가 특정 게시글에 좋아요를 눌렀는지 확인
    Optional<PostLike> findByPostAndUser(Post post, Users user);

    // ✅ 게시글 삭제 시 좋아요도 함께 삭제할 수 있도록
    void deleteAllByPost(Post post);

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM post_likes WHERE post_id = ?1", nativeQuery = true)
    void deleteByPostId(Long postId);

    // 🔥 특정 게시글의 좋아요 수 카운트
    long countByPost(Post post);
}
