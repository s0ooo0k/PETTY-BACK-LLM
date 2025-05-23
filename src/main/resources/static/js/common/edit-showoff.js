let originalImages = [];
let postType = "SHOWOFF";

const postId = new URLSearchParams(location.search).get("id");

document.addEventListener("DOMContentLoaded", () => {
  fetchPostForEdit();

  // HTML의 실제 ID와 맞춤
  const imageInput = document.getElementById("edit-showoff-imageFiles");
  if (imageInput) {
    imageInput.addEventListener("change", handleImageUpload);
  }

  const form = document.getElementById("editShowoffForm");
  if (form) {
    form.addEventListener("submit", async (e) => {
      e.preventDefault();
      const token = localStorage.getItem("jwt");

      const payload = {
        title: document.getElementById("edit-showoff-title").value,
        content: document.getElementById("edit-showoff-content").value,
        petType: getRadioValue("edit-showoff-petType") || "OTHER",
        postType: postType,
        images: originalImages
      };

      const res = await fetch(`/api/posts/${postId}`, {
        method: "PUT",
        headers: {
          "Content-Type": "application/json",
          "Authorization": `Bearer ${token}`
        },
        body: JSON.stringify(payload)
      });

      if (res.ok) {
        alert("수정 완료!");
        location.href = `/posts/detail?id=${postId}`;
      } else {
        alert("수정 실패");
      }
    });
  }
});

async function fetchPostForEdit() {
  const res = await fetch(`/api/posts/${postId}`);
  const post = await res.json();

  document.getElementById("edit-showoff-title").value = post.title;
  document.getElementById("edit-showoff-content").value = post.content;

  const petTypeInputs = document.querySelectorAll('input[name="edit-showoff-petType"]');
  petTypeInputs.forEach(input => {
    if (input.value === post.petType) {
      input.checked = true;
    }
  });

  const previewBox = document.getElementById("edit-showoff-imagePreview");
  if (previewBox) {
    (post.images || []).forEach((img, index) => {
      const imgWrapper = document.createElement("div");
      imgWrapper.style.display = "inline-block";
      imgWrapper.style.margin = "5px";
      imgWrapper.innerHTML = `
        <img src="${img.imageUrl}" data-url="${img.imageUrl}" style="max-width: 100px; border-radius: 6px; object-fit: cover;">
        <button type="button" onclick="removeImage('${img.imageUrl}')" style="display: block; margin-top: 5px;">삭제</button>
      `;
      previewBox.appendChild(imgWrapper);

      originalImages.push({
        id: img.id,
        imageUrl: img.imageUrl,
        ordering: img.ordering,
        isDeleted: false
      });
    });
  }
}

function getRadioValue(name) {
  const radios = document.querySelectorAll(`input[name="${name}"]`);
  for (const radio of radios) {
    if (radio.checked) return radio.value;
  }
  return null;
}

async function handleImageUpload(e) {
  const files = Array.from(e.target.files);
  if (!files.length) return;

  const currentCount = originalImages.filter(img => !img.isDeleted).length;
  const maxCount = 5;
  if (currentCount >= maxCount) {
    alert("최대 5개의 이미지를 업로드할 수 있습니다.");
    return;
  }

  const availableSlots = maxCount - currentCount;
  const filesToUpload = files.slice(0, availableSlots);

  const formData = new FormData();
  for (const file of filesToUpload) {
    formData.append("files", file);
  }

  const token = localStorage.getItem("jwt");
  const res = await fetch('/api/images/upload/multi', {
    method: 'POST',
    headers: {
      'Authorization': `Bearer ${token}`
    },
    body: formData
  });

  if (!res.ok) {
    alert("이미지 업로드 실패");
    return;
  }

  const json = await res.json();
  const previewBox = document.getElementById("edit-showoff-imagePreview");

  if (previewBox && json.images) {
    json.images.forEach((img) => {
      if (originalImages.some(existing => existing.imageUrl === img.imageUrl)) return;

      originalImages.push(img);

      const imgWrapper = document.createElement("div");
      imgWrapper.style.display = "inline-block";
      imgWrapper.style.margin = "5px";
      imgWrapper.innerHTML = `
        <img src="${img.imageUrl}" data-url="${img.imageUrl}" style="max-width: 100px; border-radius: 6px; object-fit: cover;">
        <button type="button" onclick="removeImage('${img.imageUrl}')" style="display: block; margin-top: 5px;">삭제</button>
      `;
      previewBox.appendChild(imgWrapper);
    });
  }
}

function removeImage(url) {
  const img = originalImages.find(img => img.imageUrl === url);
  if (img) {
    img.isDeleted = true;
  }

  const wrapper = document.querySelector(`img[data-url='${url}']`)?.parentElement;
  if (wrapper) wrapper.remove();
}