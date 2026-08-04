export const MAX_MESSAGE_IMAGES = 5;
export const MAX_MESSAGE_IMAGE_BYTES = 10 * 1024 * 1024;

const ALLOWED_IMAGE_TYPES = new Set(['image/jpeg', 'image/jpg', 'image/png', 'image/webp']);

/** Kiểm tra ảnh ngay trên giao diện để phản hồi sớm; Backend vẫn là lớp xác thực cuối cùng. */
export function validateMessageImages(currentFiles = [], incomingFiles = []) {
  const incoming = Array.from(incomingFiles ?? []);
  if (currentFiles.length + incoming.length > MAX_MESSAGE_IMAGES) {
    return { files: currentFiles, error: `Mỗi tin nhắn chỉ được đính kèm tối đa ${MAX_MESSAGE_IMAGES} ảnh.` };
  }

  const invalidType = incoming.find((file) => !ALLOWED_IMAGE_TYPES.has(file.type));
  if (invalidType) {
    return { files: currentFiles, error: 'Chỉ hỗ trợ ảnh JPG, JPEG, PNG hoặc WEBP.' };
  }

  const emptyFile = incoming.find((file) => file.size <= 0);
  if (emptyFile) return { files: currentFiles, error: 'Ảnh đính kèm không được rỗng.' };

  const oversized = incoming.find((file) => file.size > MAX_MESSAGE_IMAGE_BYTES);
  if (oversized) return { files: currentFiles, error: 'Mỗi ảnh phải có dung lượng không quá 10 MB.' };

  return { files: [...currentFiles, ...incoming], error: '' };
}

/** FormData dùng cùng contract multipart của endpoint gửi tin nhắn ảnh. */
export function createImageMessageFormData({ clientMessageId, content, images }) {
  const formData = new FormData();
  formData.append('clientMessageId', clientMessageId);
  if (content?.trim()) formData.append('content', content);
  images.forEach((image) => formData.append('images', image));
  return formData;
}
