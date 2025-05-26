package io.github.petty.community.service;

import io.github.petty.community.dto.*;
import io.github.petty.users.entity.Users;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PostService {
    Long save(PostRequest request, Users user);
    void update(Long id, PostRequest request, Users user);
    String delete(Long id, Users user); // 🔥 삭제된 게시글의 타입 반환
    Page<?> findAllByType(String type, Pageable pageable);
    PostDetailResponse findById(Long id);
    int toggleLike(Long postId, Users user);
    void updateAllPostCounts(); // 🔥 모든 게시글의 댓글 수와 좋아요 수 업데이트
}