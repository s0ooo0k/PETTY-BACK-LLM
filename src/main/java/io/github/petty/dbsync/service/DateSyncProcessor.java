package io.github.petty.dbsync.service;

import io.github.petty.dbsync.client.TourApiClient;
import io.github.petty.dbsync.dto.*;
import io.github.petty.tour.entity.Content;
import io.github.petty.tour.entity.SyncStatus;
import io.github.petty.dbsync.mapper.DataMapper;
import io.github.petty.tour.repository.ContentRepository;
import io.github.petty.tour.repository.SyncStatusRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.codec.DecodingException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DateSyncProcessor {
    private final TourApiClient tourApiClient;
    private final ContentRepository contentRepository;
    private final DataMapper dataMapper;
    private final SyncStatusRepository syncStatusRepository; // 상태 업데이트 위해 필요

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");


    /**
     * 지정된 날짜 하루치에 대한 데이터 동기화를 수행합니다 (DELETE 후 INSERT/UPDATE).
     * 성공 시 SyncStatus 테이블의 lastSyncDate를 업데이트 합니다.
     * 이 메소드 전체가 하나의 트랜잭션으로 묶입니다.
     * @param date 동기화할 날짜
     */
    @Transactional // 전체 메소드를 하나의 트랜잭션으로 실행
    public void syncForDate(LocalDate date) {
        String dateString = date.format(DATE_FORMATTER);
        log.debug("[{}] 동기화 처리 시작.", dateString);

        List<PetTourSyncItemDto> deletionCandidates = Collections.emptyList();
        List<PetTourSyncItemDto> insertionOrUpdateCandidates = Collections.emptyList();
        boolean apiErrorOccurred = false;


        // --- Step 0a: 삭제 대상 후보 조회 (API 호출 및 예외 처리) ---
        log.debug("[{}] 삭제 대상 목록 API 조회를 시작", dateString);
        try {

            deletionCandidates =
                    tourApiClient.fetchPetTourSyncList(dateString, null, 1, 1000)
                    .blockOptional().orElse(Collections.emptyList());
            log.info("[{}] 삭제 대상 후보 {}건 조회 완료.", dateString, deletionCandidates.size());


        } catch (DecodingException e) {
            deletionCandidates = handleApiDecodingError(e, dateString, "삭제 대상 조회", true);
            if (deletionCandidates == null) { // handleApiDecodingError가 null을 반환하면 심각한 오류로 간주 (실제로는 List이므로 null 반환 안함)
                apiErrorOccurred = true;
            }
        } catch (Exception e) {
            // 네트워크 오류 등 API 호출 자체의 다른 오류 발생 시
            log.error("[{}] API 삭제 대상 조회 중 오류 발생", dateString, e);
            apiErrorOccurred = true; // 오류 플래그 설정
        }

        // --- Step 0b: INSERT/UPDATE 대상 후보 조회 (showflag=1) ---
        if (!apiErrorOccurred) {
            log.debug("[{}] INSERT/UPDATE 대상 (showflag=1) 목록 API 조회를 시작합니다.", dateString);
            try {
                insertionOrUpdateCandidates =
                        tourApiClient.fetchPetTourSyncList(dateString, "1", 1, 1000) // showflag=1 사용
                        .blockOptional().orElse(Collections.emptyList());
                log.info("[{}] INSERT/UPDATE 대상 (showflag=1) {}건 조회 완료.", dateString, insertionOrUpdateCandidates.size());
            } catch (DecodingException e) {
                insertionOrUpdateCandidates = handleApiDecodingError(e, dateString, "INSERT/UPDATE 대상(1) 조회", true);
                if (insertionOrUpdateCandidates == null) apiErrorOccurred = true;
            } catch (Exception e) {
                log.error("[{}] API INSERT/UPDATE 대상(1) 조회 중 예측 못한 오류 발생!", dateString, e);
                apiErrorOccurred = true;
            }
        }

        // API 조회/처리 중 심각한 오류 발생 시 여기서 롤백 및 종료
        if (apiErrorOccurred) {
            throw new RuntimeException("API 목록 처리 중 심각한 오류 발생: " + dateString);
        }

        boolean dbOperationsSuccessful = false;
        try {
            // --- 1단계: 실제 삭제 대상 ID 결정 ---
            List<Long> contentIdsToDelete = deletionCandidates.stream()
                    .map(PetTourSyncItemDto::getContentId)
                    .filter(Objects::nonNull)
                    .distinct()
                    .collect(Collectors.toList());
            log.info("[{}] 실제 삭제 대상  ID {}건.", dateString, contentIdsToDelete.size());

            // --- 2단계: 데이터 삭제 ---
            if (!contentIdsToDelete.isEmpty()) {
                log.debug("[{}] {}건의 데이터 삭제를 시작합니다.", dateString, contentIdsToDelete.size());
                try {
                    long deletedCount = contentRepository.deleteAllByContentIdIn(contentIdsToDelete);
                    log.info("[{}] 데이터 {}건 삭제 요청 완료. 실제 삭제 수: {}", dateString, contentIdsToDelete.size(), deletedCount);
                } catch (Exception e) {
                    log.error("[{}] 데이터 삭제 중 오류 발생!", dateString, e);
                    throw new RuntimeException("데이터 삭제 단계 실패: " + dateString, e); // 롤백 유도
                }
            } else {
                log.info("[{}] 삭제할 데이터가 없습니다.", dateString);
            }

            // --- 3단계 & 4단계: INSERT/UPDATE 처리 (Fetch-then-Update 적용) ---
            if (!insertionOrUpdateCandidates.isEmpty()) {
                log.debug("[{}] {}건의 데이터 INSERT/UPDATE 처리를 시작합니다.", dateString, insertionOrUpdateCandidates.size());
                AtomicInteger successCount = new AtomicInteger(0);
                AtomicInteger failureCount = new AtomicInteger(0);

                for (PetTourSyncItemDto itemDto : insertionOrUpdateCandidates) {
                    Long contentId = itemDto.getContentId();
                    Integer contentTypeId = itemDto.getContentTypeId();
                    if (contentId == null || contentTypeId == null) {
                        log.warn("[{}] contentId 또는 contentTypeId가 null인 항목 발견. 건너뜁니다. Item: {}", dateString, itemDto);
                        failureCount.incrementAndGet();
                        continue;
                    }

                    boolean itemProcessingError = false; // 개별 항목 처리 실패 플래그
                    try {
                        // 4a. 상세 정보 API 호출 (DecodingException 처리 완료)
                        DetailCommonDto commonDto = null;
                        DetailIntroDto introDto = null;
                        DetailPetDto petTourDto = null;
                        List<DetailInfoDto> infoDtoList = Collections.emptyList();
                        List<DetailImageDto> imageDtoList = Collections.emptyList();
                        boolean essentialApiFailed = false;

                        try { commonDto = tourApiClient.fetchDetailCommon(contentId).block(); }
                        catch (DecodingException e) { commonDto = handleApiDecodingError(e, dateString, "DetailCommon("+contentId+")", false); if(commonDto==null) essentialApiFailed=true;}
                        catch (Exception e) { log.error("[{}] API DetailCommon({}) 조회 실패", dateString, contentId, e); essentialApiFailed=true; } // 필수 API 실패

                        try { introDto = tourApiClient.fetchDetailIntro(contentId, contentTypeId).block(); }
                        catch (DecodingException e) { introDto = handleApiDecodingError(e, dateString, "DetailIntro("+contentId+")", false); /* Null 허용? */ }
                        catch (Exception e) { log.error("[{}] API DetailIntro({}) 조회 실패", dateString, contentId, e); /* Null 허용? */ }

                        try { petTourDto = tourApiClient.fetchDetailPetTour(contentId).block(); }
                        catch (DecodingException e) { petTourDto = handleApiDecodingError(e, dateString, "DetailPetTour("+contentId+")", false); /* Null 허용? */ }
                        catch (Exception e) { log.error("[{}] API DetailPetTour({}) 조회 실패", dateString, contentId, e); /* Null 허용? */ }

                        try { infoDtoList = tourApiClient.fetchDetailInfo(contentId, contentTypeId).blockOptional().orElse(Collections.emptyList()); }
                        catch (DecodingException e) { infoDtoList = handleApiDecodingError(e, dateString, "DetailInfo("+contentId+")", true); }
                        catch (Exception e) { log.error("[{}] API DetailInfo({}) 조회 실패", dateString, contentId, e); /* 빈 리스트로 계속 진행 */ }

                        try { imageDtoList = tourApiClient.fetchDetailImage(contentId).blockOptional().orElse(Collections.emptyList()); }
                        catch (DecodingException e) { imageDtoList = handleApiDecodingError(e, dateString, "DetailImage("+contentId+")", true); }
                        catch (Exception e) { log.error("[{}] API DetailImage({}) 조회 실패", dateString, contentId, e); /* 빈 리스트로 계속 진행 */ }


                        // 4a-extra. 필수 API 호출 실패 시 건너뛰기
                        if (essentialApiFailed || commonDto == null) {
                            log.warn("[{}] Skipping contentId {} due to failure in fetching essential detail data from API.", dateString, contentId);
                            itemProcessingError = true; // 실패로 기록
                        }

                        if (!itemProcessingError) {
                            log.debug("[{}] contentId {} 신규 삽입 시작.", dateString, contentId);
                            Content newContent = dataMapper.toContentEntity(commonDto, introDto, petTourDto, infoDtoList, imageDtoList);

                            if (newContent != null) {
                                // save() will persist Content and cascade to all associated children
                                contentRepository.save(newContent);
                                log.trace("[{}] contentId {} 신규 삽입 성공.", dateString, contentId);
                            } else {
                                // Handle case where mapping resulted in null (e.g., commonDto was null)
                                log.warn("[{}] contentId {} 매핑 결과가 null이므로 건너뜁니다.", dateString, contentId);
                                itemProcessingError = true; // Mark as failure if mapping fails
                            }
                        }
                        // --- Fetch-then-Update 로직 끝 ---

                    } catch (Exception e) { // API 조회 실패 또는 DB 저장 실패 등
                        itemProcessingError = true; // 실패로 기록
                        log.error("[{}] contentId {} 처리 중 오류 발생. 건너뜁니다.", dateString, contentId, e);
                    } finally {
                        if(itemProcessingError) {
                            failureCount.incrementAndGet();
                        } else {
                            successCount.incrementAndGet();
                        }
                    }
                } // end of for loop

                log.info("[{}] INSERT/UPDATE 처리 완료. 성공: {}건, 실패: {}건", dateString, successCount.get(), failureCount.get());
            } else {
                log.info("[{}] INSERT/UPDATE 대상 데이터가 없습니다.", dateString);
            }

            dbOperationsSuccessful = true; // 이 지점까지 예외 없이 도달하면 성공 간주

        } catch (Exception e) { // 삭제 단계 또는 루프 외부의 심각한 오류
            log.error("[{}] DB 처리 중 오류 발생!", dateString, e);
            dbOperationsSuccessful = false;
            // @Transactional 에 의해 롤백됨, 상위 호출자에게 알리기 위해 예외 던짐
            throw e;
        } finally {
            if (dbOperationsSuccessful) {
                updateSyncStatus(date); // 모든 작업 성공 시 상태 업데이트
            }
            log.debug("[{}] 동기화 처리 종료.", dateString);
        }
    }

    /**
     * SyncStatus 테이블의 lastSyncDate를 주어진 날짜로 업데이트합니다.
     * @param successfullySyncedDate 성공적으로 동기화된 날짜
     */
    private void updateSyncStatus(LocalDate successfullySyncedDate) {
        // 이 메소드는 syncForDate의 @Transactional 범위 내에서 호출됨
        try {
            SyncStatus status = syncStatusRepository.findById(SyncStatus.DEFAULT_ID)
                    .orElse(new SyncStatus());
            status.setLastSyncDate(successfullySyncedDate);
            syncStatusRepository.save(status);
            log.info("SyncStatus 업데이트 완료: lastSyncDate = {}", successfullySyncedDate.format(DATE_FORMATTER));
        } catch (Exception e) {
            // 상태 업데이트 실패 시, 전체 트랜잭션이 롤백되도록 RuntimeException 발생시킴
            log.error("!!!!! SyncStatus 업데이트 중 심각한 오류 발생 (lastSyncDate={}) !!!!!",
                    successfullySyncedDate.format(DATE_FORMATTER), e);
            throw new RuntimeException("SyncStatus 업데이트 실패", e); // syncForDate 트랜잭션 롤백 유도
        }
    }

    // --- Helper Method for DecodingException ---
    private <T> T handleApiDecodingError(DecodingException e, String dateString, String apiName, boolean isList) {
        if (e.getMessage() != null && e.getMessage().contains("Cannot coerce empty String") && e.getMessage().contains("ItemsDto")) {
            log.warn("[{}] API {} 응답 없음 (items가 빈 문자열). {} 반환.", dateString, apiName, isList ? "빈 리스트" : "null");
            // API 응답이 리스트 형태일 것으로 예상되면 빈 리스트 반환, 아니면 null 반환
            return isList ? (T) Collections.emptyList() : null;
        } else {
            // 예상치 못한 다른 디코딩 오류 -> 심각한 오류로 간주하고 예외 다시 던짐
            log.error("[{}] API {} 응답 처리(JSON 디코딩) 중 예상치 못한 오류 발생!", dateString, apiName, e);
            throw e;
        }
    }
}