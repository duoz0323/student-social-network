export const POST_MEDIA_LIMITS = Object.freeze({
  maxMedia: 4,
  maxVideos: 1,
  maxImageBytes: 10 * 1024 * 1024,
  maxVideoBytes: 100 * 1024 * 1024,
  maxVideoDurationSeconds: 180,
});

const SUPPORTED_IMAGE_TYPES = new Set(['image/jpeg', 'image/png', 'image/webp']);
const SUPPORTED_VIDEO_TYPES = new Set(['video/mp4', 'video/webm']);

/** Xác định loại media từ MIME type để Frontend áp dụng cùng giới hạn với Backend. */
export function mediaTypeOfFile(file) {
  if (SUPPORTED_VIDEO_TYPES.has(file.type)) return 'VIDEO';
  if (SUPPORTED_IMAGE_TYPES.has(file.type)) return 'IMAGE';
  return null;
}

/** Validate lô file mới trên tổng media cũ còn giữ và media mới đã chọn. */
export function validatePostMediaFiles(currentItems, selectedFiles) {
  const typedFiles = selectedFiles.map((file) => ({ file, mediaType: mediaTypeOfFile(file) }));
  if (typedFiles.some((item) => !item.mediaType)) {
    throw new Error('Chỉ hỗ trợ ảnh JPG, PNG, WEBP hoặc video MP4, WebM.');
  }
  if (currentItems.length + typedFiles.length > POST_MEDIA_LIMITS.maxMedia) {
    throw new Error('Mỗi bài viết chỉ được có tối đa 4 media.');
  }
  const videoCount = currentItems.filter((item) => item.mediaType === 'VIDEO').length
    + typedFiles.filter((item) => item.mediaType === 'VIDEO').length;
  if (videoCount > POST_MEDIA_LIMITS.maxVideos) {
    throw new Error('Mỗi bài viết chỉ được có tối đa 1 video.');
  }
  const oversizedItem = typedFiles.find((item) => item.file.size > (
    item.mediaType === 'VIDEO' ? POST_MEDIA_LIMITS.maxVideoBytes : POST_MEDIA_LIMITS.maxImageBytes
  ));
  if (oversizedItem) {
    throw new Error(oversizedItem.mediaType === 'VIDEO'
      ? 'Video không được vượt quá 100 MB.'
      : 'Ảnh không được vượt quá 10 MB.');
  }
  return typedFiles;
}

/** Đọc metadata video tại trình duyệt để chặn sớm video dài quá giới hạn UX. */
export function readVideoDuration(url) {
  return new Promise((resolve) => {
    const video = document.createElement('video');
    video.preload = 'metadata';
    video.onloadedmetadata = () => resolve(video.duration);
    video.onerror = () => resolve(Number.NaN);
    video.src = url;
  });
}
