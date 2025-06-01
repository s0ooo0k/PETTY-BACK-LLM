let directSearchCurrentPage = 0;
const DIRECT_SEARCH_PAGE_SIZE = 10;
let directSearchTotalPages = 0;
let directSearchSelectedLatlng = null; // 직접 검색용 좌표 저장

let map;
let geocoder;
let marker;
let infowindow;
let currentMapTargetInputId = null;
let selectedLatlng = null; // This will store the LngLat object from map click/search in the modal
let kakaoMapsApiIsReady = false;

const TOUR_TYPE_MAP_DIRECT = {
    12: '관광지', 14: '문화시설', 15: '축제', 25: '여행코스',
    28: '레포츠', 32: '숙박', 38: '쇼핑', 39: '음식점'
};
let sigunguDataCache = {};

function initializeKakaoDependentFeatures() {
    if (kakaoMapsApiIsReady) return;
    kakaoMapsApiIsReady = true;
    console.log("Kakao Maps API is confirmed ready. Initializing features.");

    geocoder = new kakao.maps.services.Geocoder();
    infowindow = new kakao.maps.InfoWindow({
        zIndex: 1,
        removable: true
    });

    const mapContainer = document.getElementById('map');
    if (mapContainer) {
        const mapOption = {
            center: new kakao.maps.LatLng(37.566826, 126.9786567),
            level: 5,
        };
        map = new kakao.maps.Map(mapContainer, mapOption);
        marker = new kakao.maps.Marker({ position: map.getCenter() });
        marker.setMap(map);

        kakao.maps.event.addListener(map, 'click', function (mouseEvent) {
            selectedLatlng = mouseEvent.latLng;
            marker.setPosition(selectedLatlng);

            searchDetailAddrFromCoords(selectedLatlng, function (result, status) {
                let content = '';
                let simpleAddr = '주소 정보 없음';
                if (status === kakao.maps.services.Status.OK && result.length > 0) {
                    simpleAddr = getSimpleAddressFromKakao(result[0]);
                    const addressName = result[0].address ? result[0].address.address_name : (result[0].road_address ? result[0].road_address.address_name : simpleAddr);
                    content = `<div class="petty-map-infowindow"><div class="info-address">${addressName}</div></div>`;
                } else {
                    content = `<div class="petty-map-infowindow"><div class="info-address">주소를 가져올 수 없습니다.</div></div>`;
                }
                infowindow.setContent(content);
                infowindow.open(map, marker);

                if (currentMapTargetInputId === 'manualLocationInput') {
                    const targetInput = document.getElementById(currentMapTargetInputId);
                    if (targetInput) {
                        targetInput.value = simpleAddr;
                    }
                }
            });
        });
    } else {
        console.error("Map container element with ID 'map' not found for initialization.");
        kakaoMapsApiIsReady = false;
    }

    const mapModalElement = document.getElementById('mapModal');
    if (mapModalElement) {
        mapModalElement.addEventListener('shown.bs.modal', function () {
            console.log("Map modal 'shown.bs.modal' event fired. currentMapTargetInputId:", currentMapTargetInputId);
            console.log("Current selectedLatlng (for modal interaction):", selectedLatlng ? selectedLatlng.toString() : "null");
            console.log("Current directSearchSelectedLatlng (for manual search):", directSearchSelectedLatlng ? directSearchSelectedLatlng.toString() : "null");

            if (map && typeof map.relayout === 'function') {
                map.relayout();
                let centerPosition;
                if (currentMapTargetInputId === 'manualLocationInput' && directSearchSelectedLatlng instanceof kakao.maps.LatLng) {
                    centerPosition = directSearchSelectedLatlng;
                } else if (selectedLatlng instanceof kakao.maps.LatLng) {
                    centerPosition = selectedLatlng;
                } else {
                    centerPosition = new kakao.maps.LatLng(37.566826, 126.9786567);
                }
                map.setCenter(centerPosition);
                if (marker) marker.setPosition(centerPosition);
            } else {
                console.error("Map object or relayout function NOT available when modal shown.");
            }
        });
    }
}

function waitForKakaoMaps(callback) {
    if (window.kakao && window.kakao.maps && window.kakao.maps.services) {
        try {
            new window.kakao.maps.services.Geocoder();
            new window.kakao.maps.services.Places();
            callback();
        } catch (e) {
            console.warn("Kakao Maps services not fully ready, retrying...", e);
            setTimeout(() => waitForKakaoMaps(callback), 200);
        }
    } else {
        console.warn("Kakao Maps SDK not available yet, retrying...");
        setTimeout(() => waitForKakaoMaps(callback), 200);
    }
}

function populateDropdown(selectElement, items, defaultOptionText = "전체") {
    if (!selectElement) return;
    selectElement.innerHTML = `<option value="" selected>${defaultOptionText}</option>`;
    if (items && items.length > 0) {
        items.forEach(item => {
            const option = document.createElement('option');
            option.value = item.code;
            option.textContent = item.name;
            selectElement.appendChild(option);
        });
        selectElement.disabled = false;
    } else {
        selectElement.disabled = true;
    }
}

function loadAreaCodes() {
    const directSearchAreaCodeSelect = document.getElementById('directSearchAreaCode');
    if (!directSearchAreaCodeSelect) return;
    const areas = [
        { code: "1", name: "서울" }, { code: "2", name: "인천" }, { code: "3", name: "대전" },
        { code: "4", name: "대구" }, { code: "5", name: "광주" }, { code: "6", name: "부산" },
        { code: "7", name: "울산" }, { code: "8", name: "세종" }, { code: "31", name: "경기도" },
        { code: "32", name: "강원특별자치도" }, { code: "33", name: "충청북도" }, { code: "34", name: "충청남도" },
        { code: "35", name: "경상북도" }, { code: "36", name: "경상남도" }, { code: "37", name: "전북특별자치도" },
        { code: "38", name: "전라남도" }, { code: "39", name: "제주특별자치도" }
    ];
    populateDropdown(directSearchAreaCodeSelect, areas, "시/도 선택");
}

function loadSigunguCodes(areaCode) {
    const directSearchSigunguCodeSelect = document.getElementById('directSearchSigunguCode');
    if (!directSearchSigunguCodeSelect) return;
    populateDropdown(directSearchSigunguCodeSelect, [], "시/군/구 선택");
    directSearchSigunguCodeSelect.disabled = true;
    if (!areaCode) return;
    if (sigunguDataCache[areaCode]) {
        populateDropdown(directSearchSigunguCodeSelect, sigunguDataCache[areaCode], "시/군/구 전체");
        return;
    }
    fetch(`/api/tour/codes?areaCode=${areaCode}`)
        .then(response => {
            if (!response.ok) throw new Error('시/군/구 정보 로드 실패: ' + response.statusText);
            return response.json();
        })
        .then(sigunguList => {
            sigunguDataCache[areaCode] = sigunguList;
            populateDropdown(directSearchSigunguCodeSelect, sigunguList, "시/군/구 전체");
        })
        .catch(error => {
            console.error(`areaCode ${areaCode}의 시/군/구 정보를 가져오는 중 오류 발생:`, error);
            populateDropdown(directSearchSigunguCodeSelect, [], "로드 실패");
        });
}

function fetchDirectSearchResults(append = false) {
    const spinner = document.getElementById('spinnerDirectSearch');
    const resultsControls = document.getElementById('resultControlsDirectSearch');
    const loadMoreContainer = document.getElementById('loadMoreContainerDirectSearch');
    const resultListDiv = document.getElementById('directSearchResultList');

    if(spinner) spinner.style.display = 'block';
    if(resultsControls) resultsControls.style.display = 'none';
    if(!append && loadMoreContainer) loadMoreContainer.style.display = 'none';

    let baseUrl = '';
    const params = new URLSearchParams();
    params.append('page', directSearchCurrentPage);
    params.append('size', DIRECT_SEARCH_PAGE_SIZE);

    const selectedContentTypeRadio = document.querySelector('input[name="directSearchContentTypeId"]:checked');
    const contentTypeId = selectedContentTypeRadio ? selectedContentTypeRadio.value : null;

    if (contentTypeId) params.append('contentTypeId', contentTypeId);

    if (document.getElementById('useCurrentLocationSwitch').checked) {
        baseUrl = '/api/tour/search/location';
        if (!directSearchSelectedLatlng) {
            if(resultListDiv) resultListDiv.innerHTML = '<p class="text-center text-warning col-12 mt-4">지도에서 현재 위치를 선택해주세요.</p>';
            if (spinner) spinner.style.display = 'none';
            if(resultsControls) resultsControls.style.display = 'flex';
            const resultCountEl = document.getElementById('resultCountDirectSearch');
            if (resultCountEl) resultCountEl.textContent = '0';
            return;
        }
        params.append('mapX', directSearchSelectedLatlng.getLng());
        params.append('mapY', directSearchSelectedLatlng.getLat());
        params.append('radius', document.getElementById('directSearchRadius').value);
        // 키워드 파라미터 제거
    } else {
        baseUrl = '/api/tour/search/area';
        const areaCode = document.getElementById('directSearchAreaCode').value;
        const sigunguCode = document.getElementById('directSearchSigunguCode').value;

        if (!areaCode) { // 키워드 조건 제거, 지역 코드 필수
            if(resultListDiv) resultListDiv.innerHTML = '<p class="text-center text-warning col-12 mt-4">시/도를 선택해주세요.</p>';
            if (spinner) spinner.style.display = 'none';
            if(resultsControls) resultsControls.style.display = 'flex';
            const resultCountEl = document.getElementById('resultCountDirectSearch');
            if (resultCountEl) resultCountEl.textContent = '0';
            return;
        }
        if (areaCode) params.append('areaCode', areaCode);
        if (sigunguCode) params.append('sigunguCode', sigunguCode);
        // 키워드 파라미터 제거
    }

    const fullUrl = `${baseUrl}?${params.toString()}`;
    console.log("Direct Search API URL:", fullUrl);

    fetch(fullUrl)
        .then(response => {
            if (spinner) spinner.style.display = 'none';
            if (!response.ok) {
                return response.text().then(text => {
                    throw new Error(`서버 응답 오류 (${response.status}): ${text || response.statusText}`);
                });
            }
            return response.json();
        })
        .then(responseData => {
            if (resultsControls) resultsControls.style.display = 'flex';
            displayDirectSearchResults(responseData.content, append);

            const resultCountEl = document.getElementById('resultCountDirectSearch');
            if (resultCountEl && responseData.page) resultCountEl.textContent = responseData.page.totalElements;
            if (responseData.page) directSearchTotalPages = responseData.page.totalPages;

            if (loadMoreContainer && responseData.page) {
                if (responseData.page.number < responseData.page.totalPages - 1) {
                    loadMoreContainer.style.display = 'block';
                } else {
                    loadMoreContainer.style.display = 'none';
                }
            }
        })
        .catch(error => {
            if (spinner) spinner.style.display = 'none';
            if(resultsControls) resultsControls.style.display = 'flex';
            const resultCountEl = document.getElementById('resultCountDirectSearch');
            if (resultCountEl) resultCountEl.textContent = '0';
            console.error('직접 검색 오류:', error);
            if (resultListDiv) resultListDiv.innerHTML = `<p class="text-center text-danger col-12">검색 결과를 가져오는 중 오류가 발생했습니다: ${error.message}</p>`;
        });
}

function displayDirectSearchResults(items, append = false) {
    const container = document.getElementById('directSearchResultList');
    if (!append) container.innerHTML = '';

    if (!items || items.length === 0) {
        if (!append) container.innerHTML = '<p class="text-center text-muted col-12 mt-4">검색된 결과가 없습니다.</p>';
        const loadMoreContainer = document.getElementById('loadMoreContainerDirectSearch');
        if (loadMoreContainer) loadMoreContainer.style.display = 'none';
        return;
    }

    items.forEach(item => {
        const colDiv = document.createElement('div');
        colDiv.className = 'col-12';
        const detailLink = `/contents/${item.contentId}`;
        const imageUrl = item.firstImage || '/assets/noimg.png';
        const title = item.title || '제목 없음';
        const addr = item.addr1 || '주소 정보 없음';
        const contentType = item.contentTypeId ? (TOUR_TYPE_MAP_DIRECT[item.contentTypeId.toString()] || '기타') : '기타';
        let distanceTag = '';
        if (item.distanceMeters && typeof item.distanceMeters === 'number') {
            distanceTag = `<span class="badge bg-light text-dark border me-1">${(item.distanceMeters / 1000).toFixed(1)}km</span>`;
        }
        colDiv.innerHTML = `
            <div class="petty-list-item d-flex mb-3">
                <div class="petty-list-item-img-wrapper">
                    <img src="${imageUrl}" alt="${title}" class="petty-list-item-img">
                </div>
                <div class="petty-list-item-content flex-grow-1">
                    <div>
                        <h5 class="petty-list-item-title mb-1"><a href="${detailLink}" target="_blank" class="text-decoration-none">${title}</a></h5>
                        <p class="petty-list-item-addr small text-muted mb-2">${addr}</p>
                    </div>
                    <div class="petty-list-item-tags">
                        <span class="badge bg-info text-dark me-1">${contentType}</span>
                        ${distanceTag}
                    </div>
                </div>
                <div class="petty-list-item-actions p-2">
                    <a href="${detailLink}" target="_blank" class="btn btn-sm btn-outline-primary">상세보기</a>
                </div>
            </div>
        `;
        container.appendChild(colDiv);
    });
}

document.addEventListener('DOMContentLoaded', function () {
    const manualLocationInput = document.getElementById('manualLocationInput');
    const keywordMapInput = document.getElementById('keywordMapInput'); // Modal's keyword input for map search
    const confirmLocationButton = document.getElementById('confirmLocationButton');

    waitForKakaoMaps(initializeKakaoDependentFeatures);

    function openMapModal(targetInputId, initialCoords) {
        if (!kakaoMapsApiIsReady) {
            alert("지도 서비스가 준비 중입니다. 잠시 후 다시 시도해주세요.");
            return;
        }
        currentMapTargetInputId = targetInputId;
        selectedLatlng = initialCoords || null;

        if(keywordMapInput) keywordMapInput.value = ''; // Clear map search keyword
        if (infowindow) infowindow.close();

        const targetInputElement = document.getElementById(targetInputId);
        const currentLocationValue = targetInputElement ? targetInputElement.value : '';

        if (!selectedLatlng && currentLocationValue && geocoder) {
            geocoder.addressSearch(currentLocationValue, function(result, status) {
                if (status === kakao.maps.services.Status.OK && result.length > 0) {
                    selectedLatlng = new kakao.maps.LatLng(result[0].y, result[0].x);
                } else {
                    selectedLatlng = new kakao.maps.LatLng(37.566826, 126.9786567);
                }
                const mapModalElement = document.getElementById('mapModal');
                if (mapModalElement) bootstrap.Modal.getOrCreateInstance(mapModalElement).show();
            });
        } else {
            if (!selectedLatlng) {
                if (targetInputId === 'manualLocationInput' && directSearchSelectedLatlng) {
                    selectedLatlng = directSearchSelectedLatlng;
                } else {
                    selectedLatlng = new kakao.maps.LatLng(37.566826, 126.9786567);
                }
            }
            const mapModalElement = document.getElementById('mapModal');
            if (mapModalElement) bootstrap.Modal.getOrCreateInstance(mapModalElement).show();
        }
    }

    if(manualLocationInput) {
        manualLocationInput.addEventListener('click', function () {
            openMapModal('manualLocationInput', directSearchSelectedLatlng);
        });
        manualLocationInput.addEventListener('blur', function() {
            const address = this.value;
            if (address && kakaoMapsApiIsReady && geocoder) {
                geocoder.addressSearch(address, function(result, status) {
                    if (status === kakao.maps.services.Status.OK && result.length > 0) {
                        directSearchSelectedLatlng = new kakao.maps.LatLng(result[0].y, result[0].x);
                        manualLocationInput.value = getSimpleAddressFromKakao(result[0]);
                    } else {
                        // directSearchSelectedLatlng = null; // Optional
                    }
                });
            } else if (!address) {
                directSearchSelectedLatlng = null;
            }
        });
    }

    const searchMapButton = document.getElementById('searchMapButton');
    if (searchMapButton) {
        searchMapButton.addEventListener('click', function () {
            if (!kakaoMapsApiIsReady) return alert("지도 서비스가 준비 중입니다.");
            const keywordVal = document.getElementById('keywordMapInput').value; // This is map's own keyword search
            if (!keywordVal.trim()) return alert('키워드를 입력해주세요!');
            const ps = new kakao.maps.services.Places();
            ps.keywordSearch(keywordVal, placesSearchCB);
        });
    }
    document.getElementById('keywordMapInput')?.addEventListener('keypress', function(e) {
        if (e.key === 'Enter') searchMapButton?.click();
    });

    if (confirmLocationButton) {
        confirmLocationButton.addEventListener('click', function () {
            const modalElement = document.getElementById('mapModal');
            const modalInstance = modalElement ? bootstrap.Modal.getInstance(modalElement) : null;

            if (currentMapTargetInputId === 'manualLocationInput' && selectedLatlng) {
                const targetInput = document.getElementById('manualLocationInput');
                if (targetInput) {
                    searchDetailAddrFromCoords(selectedLatlng, function(result, status) {
                        let finalAddress = "주소 변환 실패";
                        if (status === kakao.maps.services.Status.OK && result.length > 0) {
                            finalAddress = getSimpleAddressFromKakao(result[0]);
                        }
                        targetInput.value = finalAddress;
                        directSearchSelectedLatlng = new kakao.maps.LatLng(selectedLatlng.getLat(), selectedLatlng.getLng());
                    });
                }
            } else if (currentMapTargetInputId === 'manualLocationInput' && !selectedLatlng) {
                const targetInput = document.getElementById('manualLocationInput');
                if(targetInput) targetInput.value = '';
                directSearchSelectedLatlng = null;
            }
            if (modalInstance) modalInstance.hide();
        });
    }

    const directSearchForm = document.getElementById('directSearchForm');
    const useCurrentLocationSwitch = document.getElementById('useCurrentLocationSwitch');
    const areaSigunguFiltersDiv = document.getElementById('areaSigunguFilters');
    const currentLocationFiltersDiv = document.getElementById('currentLocationFilters');
    const directSearchAreaCodeSelect = document.getElementById('directSearchAreaCode');
    const directSearchSigunguCodeSelect = document.getElementById('directSearchSigunguCode'); // Not used, but kept for consistency
    const loadMoreButtonDirectSearch = document.getElementById('loadMoreButtonDirectSearch');

    function toggleLocationFilterUI(isCurrentLocationActive) {
        if (!areaSigunguFiltersDiv || !currentLocationFiltersDiv) return;
        if (isCurrentLocationActive) {
            areaSigunguFiltersDiv.classList.add('d-none');
            currentLocationFiltersDiv.classList.remove('d-none');
            if (directSearchAreaCodeSelect) directSearchAreaCodeSelect.disabled = true;
            if (directSearchSigunguCodeSelect) directSearchSigunguCodeSelect.disabled = true;
        } else {
            areaSigunguFiltersDiv.classList.remove('d-none');
            currentLocationFiltersDiv.classList.add('d-none');
            if (directSearchAreaCodeSelect) directSearchAreaCodeSelect.disabled = false;
            if (directSearchSigunguCodeSelect) directSearchSigunguCodeSelect.disabled = !directSearchAreaCodeSelect.value;
        }
    }

    if (useCurrentLocationSwitch) {
        toggleLocationFilterUI(useCurrentLocationSwitch.checked);
        useCurrentLocationSwitch.addEventListener('change', function () {
            toggleLocationFilterUI(this.checked);
            if (!this.checked) {
                if (manualLocationInput) manualLocationInput.value = '';
                directSearchSelectedLatlng = null;
            }
        });
    }

    if(directSearchAreaCodeSelect) {
        loadAreaCodes();
        directSearchAreaCodeSelect.addEventListener('change', function () {
            loadSigunguCodes(this.value);
        });
    }

    if (directSearchForm) {
        directSearchForm.addEventListener('submit', function (event) {
            event.preventDefault();
            directSearchCurrentPage = 0;
            const resultList = document.getElementById('directSearchResultList');
            if(resultList) resultList.innerHTML = '';
            fetchDirectSearchResults(false);
        });
    }

    if(loadMoreButtonDirectSearch){
        loadMoreButtonDirectSearch.addEventListener('click', function(){
            directSearchCurrentPage++;
            fetchDirectSearchResults(true);
        });
    }
});

function searchDetailAddrFromCoords(coords, callback) {
    if (!kakaoMapsApiIsReady || !geocoder) {
        if (callback) callback(null, kakao.maps.services.Status.ERROR);
        return;
    }
    geocoder.coord2Address(coords.getLng(), coords.getLat(), callback);
}

function placesSearchCB(data, status /*, pagination */) { // pagination not used
    if (!kakaoMapsApiIsReady || !map || !marker || !infowindow) return;
    if (status === kakao.maps.services.Status.OK) {
        const firstPlace = data[0];
        if (firstPlace) {
            selectedLatlng = new kakao.maps.LatLng(firstPlace.y, firstPlace.x);
            map.setCenter(selectedLatlng);
            marker.setPosition(selectedLatlng);
            const placeName = firstPlace.place_name;
            const addressName = firstPlace.road_address_name || firstPlace.address_name;
            const content = `<div class="petty-map-infowindow"><div class="info-title">${placeName}</div><div class="info-address">${addressName || '주소 정보 없음'}</div></div>`;
            infowindow.setContent(content);
            infowindow.open(map, marker);
            if (currentMapTargetInputId === 'manualLocationInput') {
                const targetInput = document.getElementById(currentMapTargetInputId);
                if (targetInput) targetInput.value = getSimpleAddressFromKakao(firstPlace);
            }
        } else {
            alert('검색 결과가 없습니다.');
        }
    } else if (status === kakao.maps.services.Status.ZERO_RESULT) {
        alert('검색 결과가 존재하지 않습니다.');
    } else {
        alert('키워드 검색 중 오류가 발생했습니다: ' + status);
    }
}

function getSimpleAddressFromKakao(kakaoResult) {
    let addressName = '';
    if (kakaoResult?.road_address?.address_name) addressName = kakaoResult.road_address.address_name;
    else if (kakaoResult?.address?.address_name) addressName = kakaoResult.address.address_name;
    else if (kakaoResult?.road_address_name) addressName = kakaoResult.road_address_name;
    else if (kakaoResult?.address_name) addressName = kakaoResult.address_name;
    if (!addressName) return '주소 정보 없음';
    const parts = addressName.split(' ');
    if (parts.length > 2) {
        if (parts[0].match(/(특별자치시|광역시|특별시|도|특별자치도)$/)) {
            if (parts[1].match(/(시|군|구)$/)) return `${parts[0]} ${parts[1]}`;
            if (parts.length === 1 || !parts[1].match(/(시|군|구)$/)) return parts[0];
        }
        if (parts[0] && parts[1]) {
            if (parts[1].endsWith('시') && parts.length > 2 && parts[2].endsWith('구')) return `${parts[0]} ${parts[1]} ${parts[2]}`;
            return `${parts[0]} ${parts[1]}`;
        }
    }
    return addressName;
}