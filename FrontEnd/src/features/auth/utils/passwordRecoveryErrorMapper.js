const MESSAGES = Object.freeze({
  AUTH_PASSWORD_RECOVERY_FLOW_INVALID: 'Phiên khôi phục không hợp lệ. Vui lòng bắt đầu lại.',
  AUTH_PASSWORD_RECOVERY_FLOW_EXPIRED: 'Phiên khôi phục đã hết hạn. Vui lòng bắt đầu lại.',
  AUTH_PASSWORD_RECOVERY_FLOW_ALREADY_USED: 'Phiên khôi phục không còn khả dụng. Vui lòng bắt đầu lại.',
  AUTH_PASSWORD_RECOVERY_OTP_INVALID: 'Mã xác minh chưa đúng. Vui lòng kiểm tra và thử lại.',
  AUTH_PASSWORD_RECOVERY_OTP_EXPIRED: 'Mã xác minh đã hết hạn. Vui lòng bắt đầu lại.',
  AUTH_PASSWORD_RECOVERY_OTP_ATTEMPTS_EXCEEDED: 'Bạn đã nhập sai quá số lần cho phép. Vui lòng bắt đầu lại.',
  AUTH_PASSWORD_RECOVERY_RESEND_TOO_SOON: 'Chưa đến thời điểm được gửi lại mã.',
  AUTH_PASSWORD_RESET_TOKEN_INVALID: 'Phiên đặt lại mật khẩu không hợp lệ. Vui lòng bắt đầu lại.',
  AUTH_PASSWORD_RESET_TOKEN_EXPIRED: 'Phiên đặt lại mật khẩu đã hết hạn. Vui lòng bắt đầu lại.',
  AUTH_PASSWORD_RESET_TOKEN_USED: 'Phiên đặt lại mật khẩu đã được sử dụng. Vui lòng đăng nhập hoặc bắt đầu lại.',
  AUTH_PASSWORD_MUST_BE_DIFFERENT: 'Mật khẩu mới phải khác mật khẩu hiện tại.',
  AUTH_PASSWORD_CONFIRMATION_MISMATCH: 'Mật khẩu xác nhận không khớp.',
  PASSWORD_CONFIRMATION_NOT_MATCH: 'Mật khẩu xác nhận không khớp.',
  AUTH_PASSWORD_POLICY_VIOLATION: 'Mật khẩu chưa đáp ứng chính sách bảo mật.',
  VALIDATION_ERROR: 'Thông tin chưa hợp lệ. Vui lòng kiểm tra lại.',
});

export function getPasswordRecoveryErrorMessage(error) {
  if (MESSAGES[error?.code]) return MESSAGES[error.code];
  if (error?.message === 'INVALID_PASSWORD_RECOVERY_RESPONSE' || error?.message === 'INVALID_PASSWORD_RESET_AUTHORIZATION_RESPONSE') {
    return 'Phản hồi từ máy chủ không hợp lệ. Vui lòng bắt đầu lại.';
  }
  return 'Không thể xử lý yêu cầu lúc này. Vui lòng thử lại sau.';
}
