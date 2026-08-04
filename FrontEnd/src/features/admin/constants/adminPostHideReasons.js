// Giữ nguyên mã enum AdminPostHideReason của Backend và chỉ Việt hóa nhãn hiển thị.
export const ADMIN_POST_HIDE_REASONS = Object.freeze([
  { value: 'SPAM', label: 'Spam' },
  { value: 'HARASSMENT', label: 'Quấy rối' },
  { value: 'HARMFUL_CONTENT', label: 'Nội dung gây hại' },
  { value: 'VIOLENCE', label: 'Nội dung bạo lực' },
  { value: 'MISINFORMATION', label: 'Thông tin sai lệch' },
  { value: 'SCHOOL_POLICY_VIOLATION', label: 'Vi phạm quy định nhà trường' },
  { value: 'INAPPROPRIATE_CONTENT', label: 'Nội dung không phù hợp' },
  { value: 'OTHER', label: 'Lý do khác' },
]);

export function isAdminPostHideReason(value) {
  return ADMIN_POST_HIDE_REASONS.some((reason) => reason.value === value);
}

export function getAdminPostHideReasonLabel(value) {
  return ADMIN_POST_HIDE_REASONS.find((reason) => reason.value === value)?.label || value || '—';
}
