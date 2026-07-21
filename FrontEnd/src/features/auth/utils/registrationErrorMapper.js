const REGISTRATION_MESSAGES = Object.freeze({
  INVALID_IDENTIFIER: 'Email không hợp lệ.',
  AUTH_IDENTIFIER_INVALID: 'Email không hợp lệ.',
  AUTH_PASSWORD_CONFIRMATION_MISMATCH: 'Mật khẩu xác nhận không khớp.',
  PASSWORD_CONFIRMATION_NOT_MATCH: 'Mật khẩu xác nhận không khớp.',
  AUTH_PASSWORD_POLICY_VIOLATION: 'Mật khẩu không đáp ứng chính sách bảo mật.',
  IDENTIFIER_ALREADY_REGISTERED: 'Email này đã thuộc một tài khoản.',
  AUTH_REGISTRATION_FLOW_INVALID: 'Phiên đăng ký không hợp lệ. Vui lòng bắt đầu lại.',
  AUTH_REGISTRATION_EXPIRED: 'Phiên đăng ký đã hết hạn. Vui lòng đăng ký lại.',
  REGISTRATION_EXPIRED: 'Phiên đăng ký đã hết hạn. Vui lòng đăng ký lại.',
  AUTH_REGISTRATION_CANCELLED: 'Phiên đăng ký đã được hủy.',
  AUTH_REGISTRATION_ALREADY_COMPLETED: 'Phiên đăng ký đã hoàn tất.',
  AUTH_OTP_INVALID: 'Mã OTP không đúng.',
  OTP_INVALID: 'Mã OTP không đúng.',
  AUTH_OTP_EXPIRED: 'Mã OTP đã hết hạn. Bạn có thể gửi lại mã mới.',
  OTP_EXPIRED: 'Mã OTP đã hết hạn. Bạn có thể gửi lại mã mới.',
  OTP_ALREADY_USED: 'Mã OTP này đã được sử dụng.',
  AUTH_OTP_ATTEMPTS_EXCEEDED: 'Bạn đã nhập sai OTP quá số lần cho phép. Hãy gửi mã mới.',
  OTP_ATTEMPTS_EXCEEDED: 'Bạn đã nhập sai OTP quá số lần cho phép. Hãy gửi mã mới.',
  AUTH_OTP_RESEND_TOO_SOON: 'Chưa đến thời điểm được gửi lại OTP.',
  OTP_RATE_LIMITED: 'Bạn đã yêu cầu OTP quá nhiều lần. Vui lòng thử lại sau.',
  AUTH_RATE_LIMITED: 'Có quá nhiều yêu cầu. Vui lòng thử lại sau.',
  AUTH_OTP_DELIVERY_FAILED: 'Không thể gửi OTP. Vui lòng thử gửi lại sau.',
  AUTH_REGISTRATION_START_FAILED: 'Máy chủ chưa sẵn sàng tạo đăng ký. Vui lòng kiểm tra cấu hình Auth của Backend rồi thử lại.',
  OTP_DELIVERY_FAILED: 'Không thể gửi OTP. Vui lòng thử gửi lại sau.',
  OTP_DELIVERY_UNKNOWN: 'Chưa xác định được trạng thái gửi OTP. Vui lòng chờ trước khi gửi lại.',
  NETWORK_ERROR: 'Không thể kết nối máy chủ. Dữ liệu đăng ký vẫn được giữ để bạn thử lại.',
  REQUEST_TIMEOUT: 'Yêu cầu quá thời gian. Vui lòng thử lại.',
});

export function getRegistrationErrorMessage(error) {
  return REGISTRATION_MESSAGES[error?.code] ?? error?.message ?? 'Không thể xử lý đăng ký. Vui lòng thử lại.';
}

export function isTerminalRegistrationError(error) {
  return new Set([
    'AUTH_REGISTRATION_FLOW_INVALID',
    'AUTH_REGISTRATION_EXPIRED',
    'REGISTRATION_EXPIRED',
    'AUTH_REGISTRATION_CANCELLED',
    'AUTH_REGISTRATION_ALREADY_COMPLETED',
  ]).has(error?.code);
}
