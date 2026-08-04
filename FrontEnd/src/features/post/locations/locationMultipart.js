function appendText(formData, key, value) {
  if (value !== undefined && value !== null && value !== '') formData.append(key, value);
}

function appendLocation(formData, location) {
  if (location) {
    formData.append('location', new Blob([JSON.stringify(location)], { type: 'application/json' }));
  }
}

/** Tạo multipart create Post, trong đó Location là JSON part tùy chọn. */
export function createPostForm(payload) {
  const formData = new FormData();
  appendText(formData, 'content', payload.content?.trim());
  appendText(formData, 'hashtag', payload.hashtag?.trim());
  for (const file of payload.mediaFiles ?? []) formData.append('mediaFiles', file);
  appendLocation(formData, payload.location);
  return formData;
}

/** Tạo multipart update Post với hành động Location tường minh. */
export function updatePostForm(payload) {
  const formData = new FormData();
  // Update là thay thế giá trị hiện tại; chuỗi rỗng phải được gửi để Backend có thể gỡ content/hashtag.
  formData.append('content', payload.content?.trim() ?? '');
  formData.append('hashtag', payload.hashtag?.trim() ?? '');
  if (Array.isArray(payload.keepMediaIds) && payload.keepMediaIds.length === 0) {
    // Part rỗng phân biệt "gỡ toàn bộ media" với việc không gửi field (Backend mặc định giữ toàn bộ).
    formData.append('keepMediaIds', '');
  } else {
    for (const id of payload.keepMediaIds ?? []) formData.append('keepMediaIds', id);
  }
  for (const file of payload.newMediaFiles ?? []) formData.append('newMediaFiles', file);
  formData.append('locationAction', payload.locationAction ?? 'KEEP');
  appendLocation(formData, payload.location);
  return formData;
}

export function resolveLocationUpdate(original, selected) {
  if (!original && !selected) return { locationAction: 'KEEP', location: null };
  if (original && !selected) return { locationAction: 'REMOVE', location: null };
  if (original?.placeId === selected?.placeId) return { locationAction: 'KEEP', location: null };
  return { locationAction: 'REPLACE', location: selected };
}
