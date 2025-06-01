let uploadedImages = [];

// 🔐 로그인 체크 함수 (쿠키 기반)
function isLoggedIn() {
  // HttpOnly 쿠키는 JavaScript로 직접 접근할 수 없으므로
  // 서버에 인증 상태를 확인하는 방식을 사용
  return true; // 일시적으로 true로 설정, 실제 체크는 getCurrentUser()에서
}

// 🔐 현재 로그인 사용자 정보 가져오기 (쿠키 기반)
async function getCurrentUser() {
    try {
        const res = await fetch('/api/users/me', {
            credentials: 'include' // 쿠키를 포함해서 요청
        });

        if (res.ok) {
            return await res.json();
        } else if (res.status === 401) {
            return null; // 로그인 안됨
        }
    } catch (err) {
        console.error('사용자 정보 조회 실패:', err);
    }
    return null;
}

// 🔥 추가: 공통 미리보기 컨테이너 찾기 함수
function findPreviewContainer() {
  const possibleIds = [
    'imagePreview',           // QNA 새 게시글
    'review-imagePreview',    // Review 새 게시글
    'showoff-imagePreview',   // Showoff 새 게시글
    'edit-qna-imagePreview',      // QNA 수정
    'edit-review-imagePreview',   // Review 수정
    'edit-showoff-imagePreview'   // Showoff 수정
  ];

  for (const id of possibleIds) {
    const element = document.getElementById(id);
    if (element) {
      console.log(`✅ 미리보기 컨테이너 발견: ${id}`);
      return element;
    }
  }

  console.error('❌ 미리보기 컨테이너를 찾을 수 없습니다:', possibleIds);
  return null;
}

document.addEventListener('DOMContentLoaded', async () => {
  // 🔐 페이지 로드 시 로그인 체크 (작성 페이지인 경우)
  const currentUser = await getCurrentUser();
  if (!currentUser) {
    alert("로그인이 필요한 페이지입니다.");
    window.location.replace("/login");
    return;
  }
  
  // 각 페이지별로 직접 ID를 확인하여 이벤트 리스너 등록
  const imageFileElements = [
    'imageFiles',           // QNA 페이지
    'review-imageFiles',    // Review 페이지
    'showoff-imageFiles'    // Showoff 페이지
  ];

  imageFileElements.forEach(id => {
    const element = document.getElementById(id);
    if (element) {
      element.addEventListener('change', handleImageUpload);
    }
  });

  const formElements = [
    'postForm',      // QNA 페이지
    'reviewForm',    // Review 페이지
    'showoffForm'    // Showoff 페이지
  ];

  formElements.forEach(id => {
    const element = document.getElementById(id);
    if (element) {
      element.addEventListener('submit', handleFormSubmit);
    }
  });
});

async function handleImageUpload(e) {
  // 🔐 이미지 업로드 시 로그인 체크
  const currentUser = await getCurrentUser();
  if (!currentUser) {
    showErrorMessage("로그인이 필요합니다.");
    location.href = "/login";
    return;
  }
  
  const MAX_FILE_COUNT = 5;
  const MAX_FILE_SIZE_MB = 5;

  const files = Array.from(e.target.files);

  if (files.length > MAX_FILE_COUNT) {
    showErrorMessage(`이미지는 최대 ${MAX_FILE_COUNT}장까지만 업로드할 수 있습니다.`);
    e.target.value = '';
    return;
  }

  for (const file of files) {
    if (file.size > MAX_FILE_SIZE_MB * 1024 * 1024) {
      showErrorMessage(`파일 ${file.name}은(는) 5MB를 초과합니다.`);
      e.target.value = '';
      return;
    }
  }

  const formData = new FormData();
  for (const file of files) {
    formData.append('files', file);
  }

  try {
    const res = await fetch('/api/images/upload/multi', {
      method: 'POST',
      credentials: 'include', // 쿠키 포함
      body: formData
    });

    if (!res.ok) {
      throw new Error(`서버 응답 오류: ${res.status}`);
    }

    const json = await res.json();

    if (!json.images || !Array.isArray(json.images)) {
      throw new Error("이미지 응답이 잘못되었습니다");
    }

    for (let img of json.images) {
      console.log("업로드된 이미지:", img.imageUrl);
    }

    uploadedImages.push(...json.images);

    // 🔥 수정: 공통 함수 사용으로 미리보기 컨테이너 찾기
    const previewBox = findPreviewContainer();
    if (!previewBox) {
      throw new Error("미리보기 표시에 실패했습니다");
    }

    // 🔥 수정: 중복 체크 개선 및 스타일 통일
    json.images.forEach((img) => {
      // 중복 체크: 이미 표시된 이미지인지 확인
      if (previewBox.querySelector(`img[data-url='${img.imageUrl}']`)) {
        console.log(`이미 표시된 이미지 건너뜀: ${img.imageUrl}`);
        return;
      }

      const imgWrapper = document.createElement("div");
      imgWrapper.style.display = "inline-block";
      imgWrapper.style.margin = "5px";
      imgWrapper.innerHTML = `
        <img src="${img.imageUrl}" data-url="${img.imageUrl}"
             style="max-width: 100px; border-radius: 6px; object-fit: cover;">
        <button type="button" onclick="removeUploadedImage('${img.imageUrl}')"
                style="display: block; margin-top: 5px;">삭제</button>
      `;
      previewBox.appendChild(imgWrapper);
    });

  } catch (error) {
    console.error('이미지 업로드 실패:', error);
    showErrorMessage('이미지 업로드에 실패했습니다. 잠시 후 다시 시도해주세요.');
    e.target.value = ''; // 파일 입력 리셋
  }
}

async function handleFormSubmit(e) {
  e.preventDefault();

    // 🔐 폼 제출 시 로그인 체크
    const currentUser = await getCurrentUser();
    if (!currentUser) {
      showErrorMessage("로그인이 필요합니다.");
      location.href = "/login";
      return;
    }

      const form = e.target;
      const formData = new FormData(form);

      // 🔥 방법 1: FormData에서 직접 추출 (name 속성 활용)
      const title = formData.get('title')?.trim();
      const content = formData.get('content')?.trim();
      const petType = formData.get('petType');
      const petName = formData.get('petName')?.trim();
      const region = formData.get('region')?.trim();
      const isResolved = formData.has('isResolved'); // 체크박스는 has로 확인

      // 🔐 필수 필드 검증
      if (!title) {
        showErrorMessage("제목을 입력해주세요.");
        form.querySelector('[name="title"]')?.focus();
        return;
      }

      if (!content) {
        showErrorMessage("내용을 입력해주세요.");
        form.querySelector('[name="content"]')?.focus();
        return;
      }

      if (!petType) {
        showErrorMessage("반려동물 종류를 선택해주세요.");
        return;
      }

      const postData = {
        title,
        content,
        petType,
        petName: petName || null,
        region: region || null,
        postType: detectPostType(),
        isResolved: isResolved,
        images: uploadedImages
      };

  try {
    const res = await fetch('/api/posts', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      credentials: 'include', // 쿠키 포함
      body: JSON.stringify(postData)
    });

    if (!res.ok) {
      const errorText = await res.text();
      throw new Error(`서버 응답 오류 (${res.status}): ${errorText}`);
    }

    const result = await res.json();
    showSuccessMessage('등록 완료!');
    
    // 🔥 브라우저 히스토리 조작으로 뒤로가기 문제 해결
    window.location.replace(`/posts/detail?id=${result.id}`);
    
  } catch (error) {
    console.error('등록 실패:', error);
    showErrorMessage('등록에 실패했습니다. 잠시 후 다시 시도해주세요.');
  }
}

// 🔥 사용자 친화적인 메시지 표시 함수들
function showErrorMessage(message) {
  // 기존 알림 제거
  removeExistingAlerts();
  
  const alertDiv = document.createElement('div');
  alertDiv.className = 'alert alert-error';
  alertDiv.innerHTML = `
    <span class="alert-icon">⚠️</span>
    <span class="alert-message">${message}</span>
    <button class="alert-close" onclick="this.parentElement.remove()">×</button>
  `;
  
  document.body.insertBefore(alertDiv, document.body.firstChild);
  
  // 5초 후 자동 제거
  setTimeout(() => {
    if (alertDiv.parentElement) {
      alertDiv.remove();
    }
  }, 5000);
}

function showSuccessMessage(message) {
  // 기존 알림 제거
  removeExistingAlerts();
  
  const alertDiv = document.createElement('div');
  alertDiv.className = 'alert alert-success';
  alertDiv.innerHTML = `
    <span class="alert-icon">✅</span>
    <span class="alert-message">${message}</span>
    <button class="alert-close" onclick="this.parentElement.remove()">×</button>
  `;
  
  document.body.insertBefore(alertDiv, document.body.firstChild);
  
  // 3초 후 자동 제거
  setTimeout(() => {
    if (alertDiv.parentElement) {
      alertDiv.remove();
    }
  }, 3000);
}

function removeExistingAlerts() {
  const existingAlerts = document.querySelectorAll('.alert');
  existingAlerts.forEach(alert => alert.remove());
}

// 업로드된 이미지 삭제 함수 추가
function removeUploadedImage(url) {
  const index = uploadedImages.findIndex(img => img.imageUrl === url);
  if (index > -1) {
    uploadedImages.splice(index, 1);
  }

  const wrapper = document.querySelector(`img[data-url='${url}']`)?.parentElement;
  if (wrapper) wrapper.remove();
}

function getRadioValue(name) {
  const radios = document.querySelectorAll(`input[name="${name}"]`);
  for (const radio of radios) {
    if (radio.checked) return radio.value;
  }
  return null;
}

function detectPostType() {
  if (location.pathname.includes('review')) return 'REVIEW';
  if (location.pathname.includes('qna')) return 'QNA';
  if (location.pathname.includes('showoff')) return 'SHOWOFF';
  return 'REVIEW';
}