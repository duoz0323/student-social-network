// Giữ nguyên mã enum của Backend và chỉ Việt hóa nội dung hiển thị trên giao diện quản trị.
export const ADMIN_REPORT_STATUSES = [
  { value: 'PENDING', label: 'Đang chờ' },
  { value: 'RESOLVED', label: 'Đã xử lý' },
  { value: 'REJECTED', label: 'Đã từ chối' },
];

export const ADMIN_REPORT_REASONS = [
  { value: 'SPAM', label: 'Spam' },
  { value: 'HARASSMENT', label: 'Quấy rối' },
  { value: 'HARMFUL_CONTENT', label: 'Nội dung độc hại hoặc xúc phạm' },
  { value: 'VIOLENCE', label: 'Nội dung bạo lực' },
  { value: 'MISINFORMATION', label: 'Thông tin sai lệch' },
  { value: 'INAPPROPRIATE', label: 'Nội dung không phù hợp' },
  { value: 'OTHER', label: 'Lý do khác' },
];

const ADMIN_POST_HIDE_REASON_BY_REPORT_REASON = {
  SPAM: 'SPAM',
  HARASSMENT: 'HARASSMENT',
  HARMFUL_CONTENT: 'HARMFUL_CONTENT',
  VIOLENCE: 'VIOLENCE',
  MISINFORMATION: 'MISINFORMATION',
  INAPPROPRIATE: 'INAPPROPRIATE_CONTENT',
  OTHER: 'OTHER',
};

export function getAdminReportStatusLabel(value) {
  return ADMIN_REPORT_STATUSES.find((status) => status.value === value)?.label || value || '—';
}

export function getAdminReportReasonLabel(value) {
  return ADMIN_REPORT_REASONS.find((reason) => reason.value === value)?.label || value || '—';
}

export function getAdminReportDetailStatusLabel(value) {
  if (value === 'RESOLVED') return 'Đã xử lý: Ẩn bài';
  if (value === 'REJECTED') return 'Đã xử lý: Từ chối';
  return getAdminReportStatusLabel(value);
}

export function getAdminPostHideReasonFromReportReason(value) {
  return ADMIN_POST_HIDE_REASON_BY_REPORT_REASON[value] || 'OTHER';
}
