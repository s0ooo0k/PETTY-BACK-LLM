package io.github.petty.llm.controller;

import io.github.petty.llm.service.EmbeddingBatchService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/embedding-batch")
@RequiredArgsConstructor
public class EmbeddingBatchController {
    private final EmbeddingBatchService embeddingBatchService;
    
    // 기본 저장
    @PostMapping("/run")
    public String runBatch() {
        embeddingBatchService.saveAllContentsInBatch();
        return "Vector DB 구성 완료";
    }
    
    // 실패 ID 재저장
    @PostMapping("/retry")
    public String retryFailedEmbeddings(@RequestBody List<Long> failedContentIds) {
        embeddingBatchService.retryFailedContents(failedContentIds);
        return "재시도 완료!";
    }
}
