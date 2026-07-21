const LOGIN_MESSAGES = Object.freeze({
  INVALID_IDENTIFIER: 'Email không hợp lệ.',
  VALIDATION_ERROR: 'Vui lòng kiểm tra lại thông tin đăng nhập.',
  INVALID_CREDENTIALS: 'Email hoặc mật khẩu không đúng.',
  USER_BLOCKED: 'Tài khoản đã bị khóa. Vui lòng liên hệ quản trị viên.',
  AUTH_IDENTIFIER_NOT_VERIFIED: 'Email này chưa được xác minh. Bạn có thể tiếp tục đăng ký để xác minh.',
  AUTH_PASSWORD_LOGIN_NOT_AVAILABLE: 'Email này đã tồn tại trong hệ thống.',
  AUTH_RATE_LIMITED: 'Có quá nhiều lần đăng nhập. Vui lòng chờ trước khi thử lại.',
  NETWORK_ERROR: 'Không thể kết nối máy chủ. Vui lòng kiểm tra đường truyền.',
  REQUEST_TIMEOUT: 'Yêu cầu đăng nhập quá thời gian. Vui lòng thử lại.',
  AUTH_LOGIN_FAILED: 'Không thể đăng nhập lúc này. Vui lòng thử lại sau.',
});

export function getLoginErrorMessage(error) {
  return LOGIN_MESSAGES[error?.code] ?? error?.message ?? 'Không thể đăng nhập. Vui lòng thử lại.';
}
