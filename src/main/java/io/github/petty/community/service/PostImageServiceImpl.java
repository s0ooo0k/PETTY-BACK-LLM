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
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

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
    @Transactional
    public void updateImagesFromRequest(Post post, List<PostImageRequest> imageRequests) {
        if (imageRequests == null || imageRequests.isEmpty()) {
            return;
        }

        // 1️⃣ 기존 이미지들 조회
        List<PostImage> existingImages = postImageRepository.findByPostIdOrderByOrderingAsc(post.getId());

        // 2️⃣ 삭제할 이미지들 처리
        List<Long> imagesToDelete = imageRequests.stream()
            .filter(dto -> Boolean.TRUE.equals(dto.getIsDeleted()) && dto.getId() != null)
            .map(PostImageRequest::getId)
            .toList();

        if (!imagesToDelete.isEmpty()) {
            List<PostImage> deletingImages = postImageRepository.findAllById(imagesToDelete);
            postImageRepository.deleteAll(deletingImages);
            deletingImages.forEach(image -> supabaseUploader.delete(image.getImageUrl()));
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
        Map<Long, PostImage> existingImagesMap = existingImages.stream()
                .collect(Collectors.toMap(PostImage::getId, Function.identity()));

        for (PostImageRequest dto : imageRequests) {
            if (!Boolean.TRUE.equals(dto.getIsDeleted()) && dto.getId() != null && dto.getOrdering() != null) {
                PostImage image = existingImagesMap.get(dto.getId());
                if (image != null) {
                    image.setOrdering(dto.getOrdering());
                }
            }
        }
    }
}