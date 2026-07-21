const SOCIAL_CONFLICT_MESSAGES = Object.freeze({
  AUTH_SOCIAL_CHALLENGE_INVALID: 'Phiên xử lý không hợp lệ. Vui lòng bắt đầu lại đăng nhập social.',
  AUTH_SOCIAL_CHALLENGE_EXPIRED: 'Phiên xử lý đã hết hạn. Vui lòng bắt đầu lại đăng nhập social.',
  AUTH_SOCIAL_CHALLENGE_ALREADY_USED: 'Lựa chọn này đã được xử lý. Vui lòng bắt đầu lại nếu bạn chưa đăng nhập.',
  AUTH_SOCIAL_CHALLENGE_CANCELLED: 'Phiên xử lý đã bị hủy. Vui lòng bắt đầu lại.',
  AUTH_SOCIAL_CHALLENGE_ACTION_INVALID: 'Lựa chọn này không còn phù hợp với trạng thái hiện tại.',
  AUTH_SOCIAL_PROVIDER_ALREADY_LINKED: 'Không thể sử dụng tài khoản social này.',
  AUTH_SOCIAL_ACCOUNT_CONFLICT: 'Tài khoản hiện có cần được xác minh bằng phương thức đăng nhập đã liên kết.',
  AUTH_REGISTRATION_FLOW_INVALID: 'Đăng ký đang chờ không còn hợp lệ.',
  AUTH_REGISTRATION_NOT_PENDING: 'Đăng ký đang chờ đã thay đổi trạng thái.',
  AUTH_REGISTRATION_EXPIRED: 'Đăng ký đang chờ đã hết hạn.',
  AUTH_RATE_LIMITED: 'Bạn thao tác quá nhanh. Vui lòng chờ trước khi thử lại.',
  USER_BLOCKED: 'Tài khoản đã bị khóa. Vui lòng liên hệ quản trị viên.',
  NETWORK_ERROR: 'Không thể kết nối máy chủ. Đăng ký đang chờ vẫn được giữ nguyên.',
  REQUEST_TIMEOUT: 'Chưa xác định được kết quả xử lý. Vui lòng bắt đầu lại đăng nhập social để kiểm tra an toàn.',
  INTERNAL_ERROR: 'Hệ thống đang gặp sự cố. Vui lòng thử lại sau.',
});

export const TERMINAL_SOCIAL_CONFLICT_CODES = new Set([
  'AUTH_SOCIAL_CHALLENGE_INVALID',
  'AUTH_SOCIAL_CHALLENGE_EXPIRED',
  'AUTH_SOCIAL_CHALLENGE_ALREADY_USED',
  'AUTH_SOCIAL_CHALLENGE_CANCELLED',
]);

export function getSocialConflictErrorMessage(error) {
  return SOCIAL_CONFLICT_MESSAGES[error?.code] ?? 'Không thể xử lý lựa chọn. Vui lòng thử lại sau.';
}
