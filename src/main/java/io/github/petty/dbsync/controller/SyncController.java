package io.github.petty.dbsync.controller;

import io.github.petty.dbsync.service.SyncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/manual-sync") // API 경로 설정
@RequiredArgsConstructor
public class SyncController {

    private final SyncService syncService;

    @PostMapping("/run")
    public ResponseEntity<String> runManualSync() {
        log.info("===== 수동 동기화 요청 수신 =====");
        try {
            syncService.synchronizePetTourData();
            log.info("===== 수동 동기화 요청 처리 완료 =====");
            return ResponseEntity.ok("수동 동기화 작업이 성공적으로 시작/완료되었습니다."); // 실제 완료는 비동기일 수 있음
        } catch (Exception e) {
            log.error("!!!!! 수동 동기화 실행 중 오류 발생 !!!!!", e);
            return ResponseEntity.internalServerError().body("수동 동기화 실행 중 오류 발생: " + e.getMessage());
        }
    }
}
