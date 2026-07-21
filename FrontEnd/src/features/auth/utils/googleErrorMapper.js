const GOOGLE_MESSAGES = Object.freeze({
  AUTH_GOOGLE_TOKEN_REQUIRED: 'Google không trả thông tin xác thực. Vui lòng thử lại.',
  AUTH_GOOGLE_TOKEN_INVALID: 'Không thể xác minh đăng nhập Google. Vui lòng thử lại.',
  AUTH_GOOGLE_TOKEN_EXPIRED: 'Phiên xác thực Google đã hết hạn. Vui lòng chọn lại tài khoản.',
  AUTH_GOOGLE_AUDIENCE_INVALID: 'Không thể xác minh đăng nhập Google cho ứng dụng này.',
  AUTH_GOOGLE_ISSUER_INVALID: 'Không thể xác minh nguồn đăng nhập Google.',
  AUTH_GOOGLE_EMAIL_NOT_VERIFIED: 'Email Google chưa được xác minh.',
  AUTH_GOOGLE_EMAIL_MISSING: 'Tài khoản Google không cung cấp email cần thiết.',
  AUTH_GOOGLE_PROVIDER_CONFLICT: 'Tài khoản Google này đã thuộc một tài khoản khác.',
  AUTH_SOCIAL_ACCOUNT_CONFLICT: 'Cần xác minh cách sử dụng tài khoản hiện có trước khi tiếp tục.',
  AUTH_SOCIAL_PENDING_CONFLICT: 'Đăng ký đang chờ cần được xử lý trước khi tiếp tục với Google.',
  USER_BLOCKED: 'Tài khoản đã bị khóa. Vui lòng liên hệ quản trị viên.',
  AUTH_RATE_LIMITED: 'Có quá nhiều yêu cầu xác thực. Vui lòng thử lại sau.',
  AUTH_GOOGLE_AUTHENTICATION_FAILED: 'Server chưa được cấu hình đúng cho Google. Vui lòng kiểm tra Google Client ID và cấu hình Auth.',
  NETWORK_ERROR: 'Không thể kết nối server để xác minh Google.',
  REQUEST_TIMEOUT: 'Xác minh Google quá thời gian. Vui lòng thử lại.',
  GOOGLE_POPUP_CLOSED: '',
  GOOGLE_PROMPT_UNAVAILABLE: 'Không thể mở đăng nhập Google. Vui lòng thử lại.',
  GOOGLE_LOGIN_TIMEOUT: 'Đăng nhập Google quá thời gian. Vui lòng thử lại.',
  GOOGLE_CREDENTIAL_MISSING: 'Google không trả thông tin xác thực. Vui lòng thử lại.',
});

export function getGoogleErrorMessage(error) {
  return GOOGLE_MESSAGES[error?.code ?? error?.message] ?? 'Không thể hoàn tất đăng nhập Google. Vui lòng thử lại.';
}
