package io.github.petty.llm.service;

import io.github.petty.tour.entity.Content;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.EmbeddingResponse;
import org.springframework.ai.vectorstore.*;
import org.springframework.ai.vectorstore.filter.Filter;
import org.springframework.stereotype.Service;
import io.github.petty.llm.dto.EmbeddingResult;
import lombok.extern.slf4j.Slf4j;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class VectorStoreService {
    private final VectorStore vectorStore;
    private final EmbeddingService embeddingService;

    // 콘텐츠를 벡터 저장소에 저장
//    public void saveContents(List<Content> contents) {
//        List<Document> documents = contents.stream()
//                .map(content -> {
//                    EmbeddingResult result = embeddingService.embedContent(content);
//                    return embeddingService.toDocument(result, content);
//                })
//                .collect(Collectors.toList());
//        log.info("embedding 완료, documents {}개 저장 시작", documents.size());
//        // Qdrant Vectorstore에 문서 추가
//        vectorStore.add(documents);
//        log.info("documents 저장 완료");
//    }

    public void saveContents(List<Content> contents) {
        List<Document> documents = contents.stream()
                .map(content -> {
                    try {
                        EmbeddingResult result = embeddingService.embedContent(content);
                        return embeddingService.toDocument(result, content);
                    } catch (IllegalArgumentException e) {
                        log.warn("Embedding 실패 - contentId: {}. 사유: {}", content.getContentId(), e.getMessage());
                        return null; // 실패한 건 버림
                    }
                })
                .filter(Objects::nonNull) // null인 건 제외
                .collect(Collectors.toList());

        if (!documents.isEmpty()) {
            log.info("embedding 완료, documents {}개 저장 시작", documents.size());
            vectorStore.add(documents);
            log.info("documents 저장 완료");
        } else {
            log.info("저장할 documents 없음 (모두 실패)");
        }
    }

    // 유사도 검색
    public List<Document> findSimilarContents(String query, int k) {
        SearchRequest searchRequest = SearchRequest.builder()
                .query(query)
                .topK(k)
                .similarityThreshold(0.1)
                .build();

        return vectorStore.similaritySearch(searchRequest);
    }

//     필터 조건을 사용한 유사 콘텐츠 검색
    public List<Document> findSimilarWithFilter(String query, int k, Filter.Expression filterExpression) {
        SearchRequest searchRequest = SearchRequest.builder()
                .query(query)
                .topK(k)
                .similarityThreshold(0.1)
                .filterExpression(filterExpression)
                .build();

        List<Document> results = vectorStore.similaritySearch(searchRequest);

        log.info("유사 콘텐츠 검색 결과");
        for (int i = 0; i < results.size(); i++) {
            Document doc = results.get(i);
            log.info("▶ 결과 {}: ID={}, Metadata={}",
                    i + 1, doc.getId(), doc.getMetadata());
        }
        return results;
    }

    // ContentID로 중복 제거
    public List<String> findAllContentIds() {
        List<Document> allDocuments = vectorStore.similaritySearch(SearchRequest.builder()
                .query("시") // 더미텍스트
                .topK(10000)
                .build());

        return allDocuments.stream()
                .map(doc -> doc.getMetadata().get("contentId").toString())
                .collect(Collectors.toList());
    }
    // 저장된 벡터 삭제
    public void deleteByIds(List<String> ids) {
        vectorStore.delete(ids);
    }
}