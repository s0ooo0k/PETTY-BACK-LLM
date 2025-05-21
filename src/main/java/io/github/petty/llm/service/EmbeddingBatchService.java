package io.github.petty.llm.service;

import io.github.petty.tour.entity.Content;
import io.github.petty.tour.repository.ContentRepository;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmbeddingBatchService {
    private final ContentRepository contentRepository;
    private final VectorStoreService vectorStoreService;

    // Content 50개 저장
    public void saveAllContentsInBatch() {
        // 1. 상위 50개 Content 조회
        List<Content> top50Contents = contentRepository.findTop20ByOrderByContentIdAsc();

        log.info("Content 20개 가져오기 완료. 저장 시작!");

        // 2. 바로 저장 (중복 검사 없이)
        vectorStoreService.saveContents(top50Contents);

        log.info("Content 20개 저장 완료!");
    }
    
    // 기본 리스트 저장
//    public void saveAllContentsInBatch() {
//        List<Content> allContents = contentRepository.findAll();
//        int batchSize = 50;
//        int totalSize = allContents.size();
//
//        // 중간에 끊긴 번호부터 시작
////        int startIndex = 450;
////
////        // 중복 검사 = 현재 VectorStore에 저장된 contentId 리스트 가져오기
////        List<String> existingContentIds = vectorStoreService.findAllContentIds();
////
////        log.info("총 {}개 데이터, {}번부터 배치 크기 {}로 저장 시작", totalSize, startIndex, batchSize);
////
////        for (int i = startIndex; i < totalSize; i += batchSize) {
////            int end = Math.min(i + batchSize, totalSize);
////            List<Content> batchList = allContents.subList(i, end);
////
////            log.info("{}번째 batch 시작 ({} ~ {})", (i / batchSize) + 1, i, end);
////
////            // 중복이면 제거하기
////            List<Content> filteredBatch = batchList.stream()
////                    .filter(content -> {
////                        String contentId = content.getContentId().toString();
////                        return !existingContentIds.contains(contentId);
////                    })
////                    .toList();
////
////            if (filteredBatch.isEmpty()) {
////                log.info("{}번째 batch: 저장할 새 데이터 없음 (모두 중복)", (i / batchSize) + 1);
////                continue;
////            }
////
////            // 3. 저장
////            vectorStoreService.saveContents(filteredBatch);
////
////            log.info("{}번째 batch 저장 완료 ({}개 저장)", (i / batchSize) + 1, filteredBatch.size());
////        }
////    }
//
//    // 실패한 리스트 저장
////    public void retryFailedContents(List<Long> failedContentIds) {
////        List<Content> failedContents = contentRepository.findAllById(failedContentIds);
////
////        log.info("재시도할 Content 수: {}", failedContents.size());
////
////        vectorStoreService.saveContents(failedContents);
////
////        log.info("재시도 저장 완료");
////    }
}

