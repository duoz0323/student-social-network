const MODERATION_MESSAGES = Object.freeze({
  CONTENT_MODERATION_WARNING: 'Nội dung của bạn có thể chứa ngôn từ không phù hợp. Vui lòng chỉnh sửa trước khi đăng.',
  CONTENT_POLICY_VIOLATION: 'Không thể đăng nội dung này vì hệ thống phát hiện nội dung có dấu hiệu vi phạm Tiêu chuẩn cộng đồng.',
  CONTENT_MODERATION_UNAVAILABLE: 'Hiện chưa thể kiểm tra nội dung. Vui lòng thử lại sau.',
});

/** Chuẩn hóa wording moderation và không hiển thị confidence/model/raw provider response. */
export function getContentModerationMessage(error) {
  return MODERATION_MESSAGES[error?.code] ?? null;
}

export function isContentModerationError(error) {
  return getContentModerationMessage(error) !== null;
}
