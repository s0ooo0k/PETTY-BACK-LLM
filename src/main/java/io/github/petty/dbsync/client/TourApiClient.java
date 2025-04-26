package io.github.petty.dbsync.client;

import io.github.petty.dbsync.config.TourProperties;
import io.github.petty.dbsync.dto.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriBuilder;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Objects;


@Component
@Slf4j
public class TourApiClient {

    private final WebClient webClient;
    private final TourProperties tourProperties; // API 설정값 주입

    // 생성자를 통한 WebClient 및 설정 주입
    public TourApiClient(WebClient webClient, TourProperties tourProperties) {
        // WebClientConfig 등에서 생성된 WebClient Bean 주입
        this.webClient = webClient;
        this.tourProperties = tourProperties;
    }

    // OpenApiClient 내부에 추가될 메소드들

    /**
     * 반려동물 동반여행 동기화 목록 조회
     * @param modifiedtime YYYYMMDD 형식의 수정일자 (필수)
     * @param showflag '1'이면 활성 목록, null이면 전체 변경 목록
     * @param pageNo 페이지 번호 (기본값 1)
     * @param numOfRows 페이지당 결과 수 (기본값 10 or 1000 등)
     * @return PetTourSyncItemDto 리스트를 포함하는 Mono
     */
    public Mono<List<PetTourSyncItemDto>> fetchPetTourSyncList(String modifiedtime, String showflag, int pageNo, int numOfRows) {
        // WebClient GET 요청 시작
        return webClient.get()
                .uri(uriBuilder -> { // URI 및 쿼리 파라미터 설정
                    // 1. API 엔드포인트 경로 설정 (실제 API 명세에 맞게 수정 필요), 필수 파라미터 추가
                    uriBuilder.path("/petTourSyncList");
                    uriBuilder.queryParam("serviceKey", tourProperties.getServiceKey());
                    uriBuilder.queryParam("pageNo", pageNo);
                    uriBuilder.queryParam("numOfRows", numOfRows);

                    // 3. 선택 파라미터 추가 (showflag가 null이나 빈 문자열이 아닐 경우)
                    if (showflag != null && !showflag.trim().isEmpty()) {
                        uriBuilder.queryParam("showflag", showflag); // '1'이면 활성 목록
                    }
                    if (modifiedtime != null && !modifiedtime.trim().isEmpty()) {
                        uriBuilder.queryParam("modifiedtime", modifiedtime);
                    }

                    addDefaultParams(uriBuilder);
                    URI uri = uriBuilder.build();
                    log.debug("Request URI: {}", uri.toString());
                    return uri;
                })
                .retrieve() // 요청 실행 및 응답 수신 준비
                // API 응답 전체를 Wrapper DTO로 변환 (Item이 List 형태임에 유의)
                .bodyToMono(new ParameterizedTypeReference<ApiResponseDto<BodyDto<ItemsDto<PetTourSyncItemDto>>>>() {})
                .flatMap(this::handleListResponse);

    }

    /**
     * 공통 정보 조회
     * @param contentId 콘텐츠 ID (필수)
     * @return DetailCommonDto를 포함하는 Mono
     */
    public Mono<DetailCommonDto> fetchDetailCommon(long contentId) {
        return webClient.get()
                .uri(uriBuilder -> {
                    uriBuilder.path("/detailCommon"); // Endpoint
                    uriBuilder.queryParam("serviceKey", tourProperties.getServiceKey());
                    uriBuilder.queryParam("contentId", contentId);
                    // YN Flags as per JS reference
                    uriBuilder.queryParam("defaultYN", "Y");
                    uriBuilder.queryParam("firstImageYN", "Y");
                    uriBuilder.queryParam("areacodeYN", "Y");
                    uriBuilder.queryParam("catcodeYN", "Y");
                    uriBuilder.queryParam("addrinfoYN", "Y");
                    uriBuilder.queryParam("mapinfoYN", "Y");
                    uriBuilder.queryParam("overviewYN", "Y");

                    // Default parameters
                    addDefaultParams(uriBuilder);
                    return uriBuilder.build();
                })
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<ApiResponseDto<BodyDto<ItemsDto<DetailCommonDto>>>>() {})
                .flatMap(this::handleListResponse)
                .flatMap(list -> { // 리스트에서 첫 번째 요소 추출 (이 로직은 동일)
                    if (list != null && !list.isEmpty()) {
                        return Mono.just(list.get(0));
                    } else {
                        log.debug("fetchDetailCommon for contentId {} returned success but no items found.", contentId);
                        return Mono.empty();
                    }
                });

    }

    /**
     * 소개 정보 조회 (detailIntro)
     * @param contentId 콘텐츠 ID (필수)
     * @param contentTypeId 콘텐츠 타입 ID (필수)
     * @return DetailIntroDto를 포함하는 Mono
     */
    public Mono<DetailIntroDto> fetchDetailIntro(long contentId, int contentTypeId) {
        return webClient.get()
                .uri(uriBuilder -> {
                    uriBuilder.path("/detailIntro");
                    uriBuilder.queryParam("serviceKey", tourProperties.getServiceKey());
                    uriBuilder.queryParam("contentId", contentId);
                    uriBuilder.queryParam("contentTypeId", contentTypeId);
                    addDefaultParams(uriBuilder);
                    return uriBuilder.build();
                })
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<ApiResponseDto<BodyDto<ItemsDto<DetailIntroDto>>>>() {})
                .flatMap(this::handleListResponse)
                .flatMap(list -> { // 리스트에서 첫 번째 요소 추출 (이 로직은 동일)
                    if (list != null && !list.isEmpty()) {
                        return Mono.just(list.get(0));
                    } else {
                        log.debug("fetchDetailIntro for contentId {} returned success but no items found.", contentId);
                        return Mono.empty();
                    }
                });
    }

    /**
     * 반복 정보 조회 (숙박/기타) (detailInfo)
     * @param contentId 콘텐츠 ID (필수)
     * @param contentTypeId 콘텐츠 타입 ID (필수)
     * @return DetailInfoDto 리스트를 포함하는 Mono
     */
    public Mono<List<DetailInfoDto>> fetchDetailInfo(long contentId, int contentTypeId) {
        return webClient.get()
                .uri(uriBuilder -> {
                    uriBuilder.path("/detailInfo");
                    uriBuilder.queryParam("serviceKey", tourProperties.getServiceKey());
                    uriBuilder.queryParam("contentId", contentId);
                    uriBuilder.queryParam("numOfRows", 50);
                    uriBuilder.queryParam("contentTypeId", contentTypeId);
                    addDefaultParams(uriBuilder);
                    return uriBuilder.build();
                })
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<ApiResponseDto<BodyDto<ItemsDto<DetailInfoDto>>>>() {})
                .flatMap(this::handleListResponse);
    }

    /**
     * 이미지 정보 조회 (detailImage)
     * @param contentId 콘텐츠 ID (필수)
     * @return DetailImageDto 리스트를 포함하는 Mono
     */
    public Mono<List<DetailImageDto>> fetchDetailImage(long contentId) {
        return webClient.get()
                .uri(uriBuilder -> {
                    uriBuilder.path( "/detailImage");
                    uriBuilder.queryParam("serviceKey", tourProperties.getServiceKey());
                    uriBuilder.queryParam("contentId", contentId);
                    uriBuilder.queryParam("numOfRows", 50);
                    uriBuilder.queryParam("imageYN", "Y");
                    addDefaultParams(uriBuilder);
                    return uriBuilder.build();
                })
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<ApiResponseDto<BodyDto<ItemsDto<DetailImageDto>>>>() {})
                .flatMap(this::handleListResponse);
    }

    /**
     * 반려동물 동반 정보 조회 (detailPetTour)
     * @param contentId 콘텐츠 ID (필수)
     * @return DetailPetDto를 포함하는 Mono
     */
    public Mono<DetailPetDto> fetchDetailPetTour(long contentId) {
        return webClient.get()
                .uri(uriBuilder -> {
                    uriBuilder.path("/detailPetTour");
                    uriBuilder.queryParam("serviceKey", tourProperties.getServiceKey());
                    uriBuilder.queryParam("contentId", contentId);
                    addDefaultParams(uriBuilder);
                    return uriBuilder.build();
                })
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<ApiResponseDto<BodyDto<ItemsDto<DetailPetDto>>>>() {})
                .flatMap(this::handleListResponse)
                .flatMap(list -> { // 리스트에서 첫 번째 요소 추출 (이 로직은 동일)
                    if (list != null && !list.isEmpty()) {
                        return Mono.just(list.get(0));
                    } else {
                        log.debug("fetchDetailPetTour for contentId {} returned success but no items found.", contentId);
                        return Mono.empty();
                    }
                });
    }

    // --- Private Helper 메소드들 ---
    private void addDefaultParams(UriBuilder uriBuilder) {
        Map<String, String> defaultParams = tourProperties.getDefaultParams();
        if (defaultParams != null) {
            defaultParams.forEach(uriBuilder::queryParam);
        }
    }


    private <T> Mono<List<T>> handleListResponse(ApiResponseDto<BodyDto<ItemsDto<T>>> apiResponse) {
        if (apiResponse.getResponse() == null || apiResponse.getResponse().getHeader() == null) {
            log.warn("Invalid API response structure: Response or Header is null");
            return Mono.just(List.of());
        }
        HeaderDto header = apiResponse.getResponse().getHeader();
        if (!"0000".equals(header.getResultCode())) {
            log.warn("API Error encountered - Code: {}, Message: {}", header.getResultCode(), header.getResultMsg());

            return Mono.just(List.of()); // Return empty list on API error
        }

        // Extract list of items (Null-safety)
        List<T> items = null;
        if (apiResponse.getResponse().getBody() != null &&
                apiResponse.getResponse().getBody().getItems() != null) {
            items = apiResponse.getResponse().getBody().getItems().getItem();
        }

        // Return items if present, otherwise an empty list
        return Mono.just(Objects.requireNonNullElseGet(items, List::of));
    }

}