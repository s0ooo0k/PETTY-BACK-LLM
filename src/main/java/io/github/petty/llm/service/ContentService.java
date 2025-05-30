package io.github.petty.llm.service;


import io.github.petty.tour.dto.DetailPetDto;
import io.github.petty.tour.entity.Content;
import io.github.petty.tour.entity.ContentImage;
import io.github.petty.tour.repository.ContentImageRepository;
import io.github.petty.tour.repository.ContentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ContentService {
    // content ID 받아서 추가 정보 받기
    private final ContentRepository contentRepository;
    private final ContentImageRepository contentImageRepository;

    private static final String DEFAULT_IMAGE_URL = "/assets/noimg.png";

    public Optional<Content> findByContentId(String contentId){
        try {
            Long id = Long.parseLong(contentId);
            return contentRepository.findById(id);
        } catch (NumberFormatException e) {
            // contentid가 없을 때
            log.error("Invalid contentId format: {}", contentId, e);
            return Optional.empty();
        }
    }




    public String getImageUrl(String contentId){
        try {
            Long id = Long.parseLong(contentId);

            // 1. Content 엔티티에서 이미지 확인
            Optional<Content> contentOpt = contentRepository.findById(id);
            if (contentOpt.isPresent()) {
                Content content = contentOpt.get();

                // firstimage 필드가 있다고 가정 (필드명은 실제 엔티티에 맞게 조정 필요)
                String contentImage = content.getFirstImage();
                if (contentImage != null && !contentImage.isEmpty()) {
                    return contentImage;
                }
            }

            // 2. ContentImageRepository에서 이미지 검색
            List<ContentImage> images = contentImageRepository.findByContent_ContentId(id);
            if (images != null && !images.isEmpty()) {
                // 첫 번째 이미지의 URL 반환 (필드명은 실제 엔티티에 맞게 조정 필요)
                return images.get(0).getOriginImgUrl();
            }

            // 3. 이미지가 없는 경우 기본 이미지 반환
            return DEFAULT_IMAGE_URL;

        } catch (NumberFormatException e) {
            log.error("Invalid contentId format: {}", contentId, e);
            return DEFAULT_IMAGE_URL;
        }
    }


    /*
        if (pet != null) {
            sb.append("반려 동물 정보: ");
            if (pet.getAcmpyTypeCd() != null)
                sb.append("동반 유형은 ").append(pet.getAcmpyTypeCd()).append(", ");
            if (pet.getEtcAcmpyInfo() != null)
                sb.append("가능 동물: ").append(pet.getEtcAcmpyInfo()).append(", ");
            if (pet.getAcmpyPsblCpam() != null)
                sb.append("추가 정보: ").append(pet.getAcmpyPsblCpam()).append(", ");
            if (pet.getAcmpyNeedMtr() != null)
                sb.append("준비물: ").append(pet.getAcmpyNeedMtr()).append(". ");
            sb.append("\n");
        }
 */
    public Optional<DetailPetDto> getPetInfo(String contentId) {
        try {
            Long id = Long.parseLong(contentId);
            Optional<Content> contentOpt = contentRepository.findById(id);

            if (contentOpt.isPresent()) {
                Content content = contentOpt.get();
                if (content.getPetTourInfo() != null) {
                    DetailPetDto dto = new DetailPetDto();
                    dto.setContentId(id);
                    dto.setAcmpyTypeCd(content.getPetTourInfo().getAcmpyTypeCd());
                    dto.setAcmpyPsblCpam(content.getPetTourInfo().getAcmpyPsblCpam());
                    dto.setAcmpyNeedMtr(content.getPetTourInfo().getAcmpyNeedMtr());
                    return Optional.of(dto);
                }
            }
            return Optional.empty();
        } catch (NumberFormatException e) {
            log.error("반려동물 정보 관련 오류: {}", contentId, e);
            return Optional.empty();
        }
    }
}
