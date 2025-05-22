let uploadedImages = [];

document.addEventListener('DOMContentLoaded', () => {
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
  const MAX_FILE_COUNT = 5;
  const MAX_FILE_SIZE_MB = 5;

  const files = Array.from(e.target.files);

  if (files.length > MAX_FILE_COUNT) {
    alert(`이미지는 최대 ${MAX_FILE_COUNT}장까지만 업로드할 수 있습니다.`);
    e.target.value = '';
    return;
  }

  for (const file of files) {
    if (file.size > MAX_FILE_SIZE_MB * 1024 * 1024) {
      alert(`파일 ${file.name}은(는) 5MB를 초과합니다.`);
      e.target.value = '';
      return;
    }
  }

  const formData = new FormData();
  for (const file of files) {
    formData.append('files', file);
  }

  const token = localStorage.getItem('jwt');
  const res = await fetch('/api/images/upload/multi', {
    method: 'POST',
    headers: {
      'Authorization': `Bearer ${token}`
    },
    body: formData
  });

  const json = await res.json();

  if (!res.ok) {
    alert("이미지 업로드 실패: " + json.message);
    return;
  }

  if (!json.images || !Array.isArray(json.images)) {
    alert("이미지 응답이 잘못되었습니다");
    return;
  }

  for (let img of json.images) {
    console.log("업로드된 이미지:", img.imageUrl);
  }

  uploadedImages.push(...json.images);

  // 페이지별로 미리보기 컨테이너 찾기
  const previewIds = ['imagePreview', 'review-imagePreview', 'showoff-imagePreview'];
  let previewBox = null;

  for (const id of previewIds) {
    const element = document.getElementById(id);
    if (element) {
      previewBox = element;
      break;
    }
  }

  if (!previewBox) {
    console.warn('이미지 미리보기 컨테이너를 찾을 수 없습니다.');
    return;
  }

  // 이미지들 추가 (원래 방식대로)
  json.images.forEach((img) => {
    if (uploadedImages.some(existing => existing.imageUrl === img.imageUrl && existing !== img)) return;

    const imgWrapper = document.createElement("div");
    imgWrapper.innerHTML = `
      <img src="${img.imageUrl}" data-url="${img.imageUrl}" style="max-width: 100px; border-radius: 6px; object-fit: cover;">
      <button type="button" onclick="removeUploadedImage('${img.imageUrl}')">삭제</button>
    `;
    previewBox.appendChild(imgWrapper);
  });
}

async function handleFormSubmit(e) {
  e.preventDefault();

  // 페이지별로 요소 찾기
  const titleElement = document.getElementById('title') ||
                      document.getElementById('review-title') ||
                      document.getElementById('showoff-title');

  const contentElement = document.getElementById('content') ||
                        document.getElementById('review-content') ||
                        document.getElementById('showoff-content');

  const petNameElement = document.getElementById('petName');
  const regionElement = document.getElementById('region');

  const postData = {
    title: titleElement?.value || '',
    content: contentElement?.value || '',
    petType: getRadioValue('petType') || getRadioValue('review-petType') || getRadioValue('showoff-petType') || 'OTHER',
    petName: petNameElement?.value || null,
    region: regionElement?.value || null,
    postType: detectPostType(),
    isResolved: false,
    images: uploadedImages
  };

  const token = localStorage.getItem('jwt');
  const res = await fetch('/api/posts', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${token}`
    },
    body: JSON.stringify(postData)
  });

  if (res.ok) {
    const { id } = await res.json();
    alert('등록 완료!');
    location.href = `/posts/detail?id=${id}`;
  } else {
    alert('등록 실패 😢');
  }
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