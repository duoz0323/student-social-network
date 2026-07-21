const FACEBOOK_MESSAGES = Object.freeze({
  FACEBOOK_APP_ID_MISSING: 'Đăng nhập Facebook chưa được cấu hình.',
  FACEBOOK_SDK_LOAD_FAILED: 'Không thể tải Facebook SDK. Vui lòng kiểm tra kết nối và thử lại.',
  FACEBOOK_SDK_TIMEOUT: 'Facebook SDK tải quá lâu. Vui lòng thử lại.',
  FACEBOOK_SDK_UNAVAILABLE: 'Facebook SDK hiện không khả dụng. Vui lòng thử lại.',
  FACEBOOK_POPUP_CLOSED: '',
  FACEBOOK_PERMISSION_DENIED: 'Bạn chưa cấp quyền đăng nhập Facebook.',
  FACEBOOK_LOGIN_TIMEOUT: 'Đăng nhập Facebook quá thời gian. Vui lòng thử lại.',
  AUTH_FACEBOOK_TOKEN_REQUIRED: 'Facebook không trả thông tin xác thực. Vui lòng thử lại.',
  AUTH_FACEBOOK_TOKEN_INVALID: 'Không thể xác minh đăng nhập Facebook. Vui lòng thử lại.',
  AUTH_FACEBOOK_TOKEN_EXPIRED: 'Phiên xác thực Facebook đã hết hạn. Vui lòng đăng nhập lại.',
  AUTH_FACEBOOK_APP_INVALID: 'Tài khoản Facebook không dành cho ứng dụng này.',
  AUTH_FACEBOOK_USER_ID_MISSING: 'Facebook không trả định danh tài khoản hợp lệ.',
  AUTH_FACEBOOK_EMAIL_MISSING: 'Tài khoản Facebook không cung cấp email cần thiết cho luồng này.',
  AUTH_FACEBOOK_PROVIDER_CONFLICT: 'Tài khoản Facebook này đã thuộc một tài khoản khác.',
  AUTH_FACEBOOK_UNAVAILABLE: 'Facebook đang tạm thời không khả dụng. Vui lòng thử lại sau.',
  AUTH_FACEBOOK_AUTHENTICATION_FAILED: 'Không thể hoàn tất đăng nhập Facebook. Vui lòng thử lại.',
  AUTH_SOCIAL_ACCOUNT_CONFLICT: 'Cần xác minh cách sử dụng tài khoản hiện có trước khi tiếp tục.',
  AUTH_SOCIAL_PENDING_CONFLICT: 'Đăng ký đang chờ cần được xử lý trước khi tiếp tục với Facebook.',
  USER_BLOCKED: 'Tài khoản đã bị khóa. Vui lòng liên hệ quản trị viên.',
  AUTH_RATE_LIMITED: 'Có quá nhiều yêu cầu xác thực. Vui lòng thử lại sau.',
  NETWORK_ERROR: 'Không thể kết nối Backend để xác minh Facebook.',
  REQUEST_TIMEOUT: 'Xác minh Facebook quá thời gian. Vui lòng thử lại.',
});

export function getFacebookErrorMessage(error) {
  return FACEBOOK_MESSAGES[error?.code ?? error?.message]
    ?? 'Không thể hoàn tất đăng nhập Facebook. Vui lòng thử lại.';
}
