package io.github.petty.llm.service;

import io.github.petty.tour.entity.Content;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.EmbeddingResponse;
import org.springframework.ai.vectorstore.*;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class VectorStoreService {
    private final VectorStore vectorStore;
    private final EmbeddingService embeddingService;
    private final EmbeddingModel embeddingModel;

    // 콘텐츠를 벡터 저장소에 저장
    public void saveContents(List<Content> contents) {
        List<Document> documents = contents.stream()
                .map(content -> {
                    EmbeddingService.EmbeddingResult result = embeddingService.embedContent(content);
                    Map<String, Object> metadata = new HashMap<>();
                    metadata.put("contentId", result.id());
                    metadata.put("title", content.getTitle());
                    metadata.put("source", "tourContent");

                    return Document.builder()
                            .id(result.id())
                            .text(result.text())
                            .metadata(metadata)
                            .build();
                })
                .collect(Collectors.toList());

        vectorStore.add(documents);
    }

    // 유사한 콘텐츠 검색 (SearchRequest 기반)
    public List<Document> findSimilarContents(String query, int k) {
        SearchRequest searchRequest = SearchRequest.builder()
                .query(query)
                .topK(k)
                .similarityThreshold(0.3)
                .build();

        return vectorStore.similaritySearch(searchRequest);
    }

    // 필터 조건을 사용한 유사 콘텐츠 검색
    public List<Document> findSimilarWithFilter(String query, int k, String filterExpression) {
        SearchRequest searchRequest = SearchRequest.builder()
                .query(query)
                .topK(k)
                .similarityThreshold(0.3)
                .filterExpression(filterExpression)
                .build();

        return vectorStore.similaritySearch(searchRequest);
    }

    // 저장된 벡터 삭제
    public void deleteByIds(List<String> ids) {
        vectorStore.delete(ids);
    }

    // 필터 조건으로 벡터 삭제
    public void deleteByFilter(String filterExpression) {
        vectorStore.delete(filterExpression);
    }

}