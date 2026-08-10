export const ADMIN_USER_BLOCK_REASONS = Object.freeze([
  { value: 'SPAM', label: 'Spam' },
  { value: 'HARASSMENT', label: 'Quấy rối' },
  { value: 'HARMFUL_CONTENT', label: 'Nội dung gây hại' },
  { value: 'FAKE_ACCOUNT', label: 'Tài khoản giả mạo' },
  { value: 'REPEATED_VIOLATION', label: 'Vi phạm nhiều lần' },
  { value: 'PROFILE_VIOLATION', label: 'Vi phạm trang cá nhân' },
  { value: 'OTHER', label: 'Lý do khác' },
]);

export function isAdminUserBlockReason(value) {
  return ADMIN_USER_BLOCK_REASONS.some((reason) => reason.value === value);
}

export function getAdminUserBlockReasonLabel(value) {
  return ADMIN_USER_BLOCK_REASONS.find((reason) => reason.value === value)?.label || value || '—';
}
