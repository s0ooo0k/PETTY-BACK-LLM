package io.github.petty.community.service;

import io.github.petty.community.dto.PostImageRequest;
import io.github.petty.community.dto.PostImageResponse;
import io.github.petty.community.entity.Post;

import java.util.List;

public interface PostImageService {

    // ✅ 이미지 저장 (신규 게시글 생성 시)
    void saveImages(Post post, List<PostImageRequest> imageRequests);

    // ✅ 수정 요청 처리 (isDeleted, ordering 포함)
    void updateImagesFromRequest(Post post, List<PostImageRequest> imageRequests);
}
