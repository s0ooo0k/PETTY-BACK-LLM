package io.github.petty.community.service;

import io.github.petty.community.dto.PostImageRequest;
import io.github.petty.community.dto.PostImageResponse;
import io.github.petty.community.entity.Post;
import io.github.petty.community.entity.PostImage;
import io.github.petty.community.repository.PostImageRepository;
import io.github.petty.community.util.SupabaseUploader;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PostImageServiceImpl implements PostImageService {

    private final PostImageRepository postImageRepository;
    private final SupabaseUploader supabaseUploader;

    @Override
    public void saveImages(Post post, List<PostImageRequest> imageRequests) {
        if (imageRequests == null || imageRequests.isEmpty()) return;

        for (PostImageRequest dto : imageRequests) {
            PostImage image = PostImage.builder()
                    .imageUrl(dto.getImageUrl())
                    .ordering(dto.getOrdering())
                    .post(post)
                    .build();
            postImageRepository.save(image);
        }
    }

    @Override
    public void deleteImagesByPostId(Long postId) {
        postImageRepository.deleteByPostId(postId);
    }

    @Override
    public void deleteImage(Long imageId) {
        postImageRepository.deleteById(imageId);
    }

    @Override
    public List<PostImageResponse> findImageResponsesByPostId(Long postId) {
        return postImageRepository.findByPostIdOrderByOrderingAsc(postId).stream()
                .map(img -> new PostImageResponse(img.getId(), img.getImageUrl(), img.getOrdering()))
                .toList();
    }

    @Override
    @Transactional
    public void reorderImages(List<Long> orderedImageIds) {
        for (int i = 0; i < orderedImageIds.size(); i++) {
            Long imageId = orderedImageIds.get(i);
            PostImage image = postImageRepository.findById(imageId)
                    .orElseThrow(() -> new IllegalArgumentException("이미지 ID가 잘못되었습니다."));
            image.setOrdering(i);  // 새로운 순서로 업데이트
        }
    }

    @Override
    @Transactional
    public void updateImagesFromRequest(Post post, List<PostImageRequest> imageRequests) {
        if (imageRequests == null || imageRequests.isEmpty()) {
            return;
        }

        // 1️⃣ 기존 이미지들 조회
        List<PostImage> existingImages = postImageRepository.findByPostIdOrderByOrderingAsc(post.getId());
        
        // 2️⃣ 삭제할 이미지들 처리
        for (PostImageRequest dto : imageRequests) {
            if (Boolean.TRUE.equals(dto.getIsDeleted()) && dto.getId() != null) {
                // 기존 이미지 삭제
                existingImages.stream()
                    .filter(img -> img.getId().equals(dto.getId()))
                    .findFirst()
                    .ifPresent(image -> {
                        postImageRepository.delete(image);
                        supabaseUploader.delete(image.getImageUrl());
                    });
            }
        }
        
        // 3️⃣ flush로 삭제 작업 완료
        postImageRepository.flush();
        
        // 4️⃣ 새로운 이미지들 추가 (id가 없는 것들)
        for (PostImageRequest dto : imageRequests) {
            if (!Boolean.TRUE.equals(dto.getIsDeleted()) && dto.getId() == null) {
                PostImage newImage = PostImage.builder()
                    .imageUrl(dto.getImageUrl())
                    .ordering(dto.getOrdering() != null ? dto.getOrdering() : 0)
                    .post(post)
                    .build();
                postImageRepository.save(newImage);
            }
        }
        
        // 5️⃣ 기존 이미지 순서 업데이트 (삭제되지 않은 것들만)
        for (PostImageRequest dto : imageRequests) {
            if (!Boolean.TRUE.equals(dto.getIsDeleted()) && dto.getId() != null) {
                postImageRepository.findById(dto.getId())
                    .ifPresent(image -> {
                        if (dto.getOrdering() != null) {
                            image.setOrdering(dto.getOrdering());
                        }
                        // save() 호출하지 않음 - @Transactional로 자동 저장
                    });
            }
        }
    }
}

