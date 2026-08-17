const REASON_LABELS = Object.freeze({
  SPAM: 'Spam hoặc nội dung lặp lại',
  HARASSMENT: 'Quấy rối hoặc công kích người khác',
  HARMFUL_CONTENT: 'Nội dung độc hại hoặc gây nguy hiểm',
  VIOLENCE: 'Nội dung bạo lực',
  MISINFORMATION: 'Thông tin sai lệch',
  SCHOOL_POLICY_VIOLATION: 'Vi phạm quy định của nhà trường',
  INAPPROPRIATE_CONTENT: 'Nội dung không phù hợp',
  OTHER: 'Vi phạm tiêu chuẩn nội dung của hệ thống',
});

export const MODERATION_DETAIL_TYPES = Object.freeze(new Set([
  'POST_HIDDEN_BY_ADMIN',
  'CONTENT_VIOLATION_WARNING',
  'CONTENT_VIOLATION_FINAL_WARNING',
]));

export function getModerationReasonLabel(reasonCode) {
  return REASON_LABELS[reasonCode] || 'Không có lý do chi tiết được lưu cho thông báo này.';
}

export function isModerationDetailNotification(notification) {
  return MODERATION_DETAIL_TYPES.has(notification?.type);
}
