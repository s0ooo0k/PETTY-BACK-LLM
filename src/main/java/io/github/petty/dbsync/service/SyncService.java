package io.github.petty.dbsync.service;


import io.github.petty.tour.entity.SyncStatus;
import io.github.petty.tour.repository.SyncStatusRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor // final 필드에 대한 생성자 자동 생성 (Lombok)
@Slf4j
public class SyncService {

    private final SyncStatusRepository syncStatusRepository;
    private final DateSyncProcessor dateSyncProcessor;


    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final ZoneId KST_ZONE_ID = ZoneId.of("Asia/Seoul");


    @PostConstruct
    public void init() {
        log.info("SyncService 초기화 완료.");
    }

    /**
     * 매일 새벽 00:05 KST에 반려동물 동반여행 데이터 동기화 작업을 시작합니다.
     * (요구사항 FR-01)
     */

    @Scheduled(cron = "0 0 3 * * *", zone = "Asia/Seoul")
    public void synchronizePetTourData() {
        log.info("========== [시작] 반려동물 동반여행 데이터 일일 동기화 작업을 시작합니다. ==========");
        try {
            // 1. 동기화해야 할 날짜 목록 계산 (따라잡기 로직 포함)(실패 시 예외 발생)
            List<LocalDate> datesToSync = getSyncDatesToProcess();

            if (datesToSync.isEmpty()) {
                log.info("동기화할 새로운 날짜가 없습니다. 어제 날짜까지의 동기화가 완료된 상태입니다.");
                return;
            }

            log.info("동기화 대상 날짜 ({}개): {}", datesToSync.size(), datesToSync);

            // 2. 각 날짜에 대해 순차적으로 동기화 수행
            for (LocalDate date : datesToSync) {
                try {
                    log.info("--- [{}] 날짜 데이터 동기화 시작 ---", date.format(DATE_FORMATTER));
                    dateSyncProcessor.syncForDate(date); // 특정 날짜 동기화 실행
                    log.info("--- [{}] 날짜 데이터 동기화 성공 ---", date.format(DATE_FORMATTER));
                } catch (Exception e) {
                    // syncForDate 실패 시: 에러 로그 남기고 예외를 다시 던짐
                    log.error("!!! [{}] 날짜 데이터 동기화 중 심각한 오류 발생. 전체 동기화 작업을 중단합니다. !!!", date.format(DATE_FORMATTER), e);
                    // 필요시 Metric 등 실패 알림 추가
                    throw e;
                }
            }
            log.info("요청된 모든 날짜({})의 동기화 처리가 완료되었습니다.", datesToSync);
        } catch (Exception e) {
            log.error("!!!!! 데이터 동기화 작업 실행 중 오류 발생하여 중단됨 !!!!!", e);
            // 필요시 Metric 등 심각 오류 알림 추가
        } finally {
            log.info("========== [종료] 반려동물 동반여행 데이터 일일 동기화 작업을 종료합니다. ==========");
        }
    }

    /**
     * 동기화해야 할 날짜 목록을 계산합니다.
     * SyncStatus 테이블에서 마지막 성공 날짜를 조회하고, 그 다음날부터 어제 날짜까지의 목록을 반환합니다.
     * (요구사항 FR-03, FR-04, FR-05)
     * @return 동기화 대상 날짜(LocalDate) 리스트
     */
    private List<LocalDate> getSyncDatesToProcess() {
        log.debug("동기화 대상 날짜 범위 계산 시작...");

        // 1. SyncStatus 테이블에서 마지막 성공 날짜 조회
        SyncStatus status = syncStatusRepository.findById(SyncStatus.DEFAULT_ID)
                .orElseThrow(() -> {
                    // 에러 로그 남기고 예외 던지기
                    log.error("!!!!!!!! 필수 SyncStatus 레코드(ID: {})를 DB에서 찾을 수 없습니다. 동기화 시작 날짜를 결정할 수 없어 동기화를 중단합니다. !!!!!!!!", SyncStatus.DEFAULT_ID);
                    return new IllegalStateException("필수 SyncStatus 레코드(ID: " + SyncStatus.DEFAULT_ID + ")가 DB에 존재하지 않습니다.");
                });

        // 2. 마지막 성공 날짜 가져오기 (status는 null 아님 보장)
        LocalDate lastSyncedDate = status.getLastSyncDate();

        // 3. 마지막 성공 날짜가 null인 경우 처리 (초기 상태) - 예외 발생
        if (lastSyncedDate == null) {
            log.error("!!!!!!!! SyncStatus 레코드(ID: {})는 찾았으나 lastSyncDate 필드가 null입니다. 초기 날짜 설정이 필요합니다. 동기화를 중단합니다. !!!!!!!!", SyncStatus.DEFAULT_ID);
            throw new IllegalStateException("SyncStatus 레코드(ID: " + SyncStatus.DEFAULT_ID + ")의 lastSyncDate가 설정되지 않았습니다. 초기 설정이 필요합니다.");
        }

        log.debug("마지막 동기화 성공 날짜: {}", lastSyncedDate.format(DATE_FORMATTER));

        // 4. 동기화 목표 마지막 날짜 결정 (어제)
        LocalDate targetEndDate = LocalDate.now(KST_ZONE_ID).minusDays(1);
        log.debug("동기화 목표 마지막 날짜: {}", targetEndDate.format(DATE_FORMATTER));


        // 5. 비교 및 날짜 목록 생성
        if (!lastSyncedDate.isBefore(targetEndDate)) {
            log.info("마지막 동기화 날짜({})가 목표 종료 날짜({})보다 같거나 이후입니다. 추가 동기화 대상이 없습니다.",
                    lastSyncedDate.format(DATE_FORMATTER), targetEndDate.format(DATE_FORMATTER));
            return Collections.emptyList(); // 동기화할 필요 없음 (정상 케이스)
        }

        // lastSyncDate 다음 날부터 targetEndDate (어제) 까지의 날짜 리스트 생성
        LocalDate nextSyncStartDate = lastSyncedDate.plusDays(1);
        List<LocalDate> datesToSync = nextSyncStartDate
                .datesUntil(targetEndDate.plusDays(1)) // datesUntil은 endExclusive 이므로 +1일
                .collect(Collectors.toList()); // Stream<LocalDate> -> List<LocalDate>

        log.info("계산된 동기화 대상 날짜 범위: {} ~ {}",
                nextSyncStartDate.format(DATE_FORMATTER), targetEndDate.format(DATE_FORMATTER));

        return datesToSync;
    }


}