const MESSAGES = Object.freeze({
  AUTH_METHOD_ALREADY_LINKED: 'Phương thức này đã được liên kết.',
  AUTH_METHOD_LINK_ALREADY_PENDING: 'Một yêu cầu liên kết đang chờ xác minh. Vui lòng thử lại sau khi flow cũ hết hạn.',
  AUTH_METHOD_LINK_CHALLENGE_INVALID: 'Phiên liên kết không hợp lệ. Vui lòng bắt đầu lại.',
  AUTH_METHOD_LINK_CHALLENGE_EXPIRED: 'Phiên liên kết đã hết hạn. Vui lòng bắt đầu lại.',
  AUTH_METHOD_LINK_CHALLENGE_ALREADY_USED: 'Phiên liên kết đã được sử dụng. Vui lòng tải lại danh sách.',
  AUTH_METHOD_LINK_OTP_INVALID: 'Mã OTP không đúng.',
  AUTH_METHOD_LINK_OTP_EXPIRED: 'Mã OTP đã hết hạn. Bạn có thể gửi lại mã nếu challenge còn hiệu lực.',
  AUTH_METHOD_LINK_OTP_ATTEMPTS_EXCEEDED: 'Bạn đã nhập sai OTP quá số lần cho phép.',
  AUTH_EMAIL_ALREADY_IN_USE: 'Email này không thể được liên kết với tài khoản hiện tại.',
  AUTH_PROVIDER_LINK_CONFLICT: 'Không thể liên kết phương thức đăng nhập này với tài khoản hiện tại.',
  AUTH_REAUTHENTICATION_REQUIRED: 'Bạn cần xác thực lại trước khi gỡ phương thức.',
  AUTH_REAUTHENTICATION_INVALID: 'Phiên xác thực lại không hợp lệ.',
  AUTH_REAUTHENTICATION_EXPIRED: 'Phiên xác thực lại đã hết hạn.',
  AUTH_REAUTHENTICATION_ALREADY_USED: 'Phiên xác thực lại đã được sử dụng.',
  AUTH_REAUTHENTICATION_CREDENTIAL_INVALID: 'Thông tin xác thực lại không đúng.',
  AUTH_REAUTHENTICATION_METHOD_UNAVAILABLE: 'Phương thức xác thực lại này chưa khả dụng.',
  AUTH_REAUTHENTICATION_PROVIDER_NOT_LINKED: 'Provider dùng để xác thực lại chưa liên kết với tài khoản.',
  AUTH_PASSWORD_ALREADY_CONFIGURED: 'Tài khoản đã có mật khẩu. Hãy dùng chức năng Đổi mật khẩu.',
  AUTH_CURRENT_PASSWORD_INCORRECT: 'Mật khẩu hiện tại không chính xác.',
  AUTH_PASSWORD_MUST_BE_DIFFERENT: 'Mật khẩu mới phải khác mật khẩu hiện tại.',
  AUTH_PASSWORD_CONFIRMATION_MISMATCH: 'Mật khẩu xác nhận không khớp.',
  AUTH_PASSWORD_POLICY_VIOLATION: 'Mật khẩu cần 8–72 ký tự, có chữ hoa, chữ thường, số và ký tự đặc biệt.',
  AUTH_IDENTIFIER_NOT_VERIFIED: 'Email chưa được xác minh.',
  AUTH_PASSWORD_LOGIN_NOT_AVAILABLE: 'Đăng nhập bằng email chưa sẵn sàng cho tài khoản này.',
  AUTH_LAST_LOGIN_METHOD_CANNOT_BE_REMOVED: 'Không thể gỡ phương thức đăng nhập cuối cùng. Hãy liên kết phương thức khác trước.',
  LAST_AUTH_METHOD: 'Không thể gỡ phương thức đăng nhập cuối cùng. Hãy liên kết phương thức khác trước.',
  AUTH_METHOD_NOT_LINKED: 'Phương thức này hiện không còn liên kết.',
  USER_BLOCKED: 'Tài khoản đã bị khóa.',
  NETWORK_ERROR: 'Không thể kết nối máy chủ. Trạng thái thao tác chưa được xác định.',
  REQUEST_TIMEOUT: 'Yêu cầu quá thời gian. Trạng thái thao tác chưa được xác định.',
});

export const isAmbiguousProviderError = (error) => error?.code === 'NETWORK_ERROR' || error?.code === 'REQUEST_TIMEOUT';
export const isTerminalLinkError = (error) => ['AUTH_METHOD_LINK_CHALLENGE_INVALID', 'AUTH_METHOD_LINK_CHALLENGE_EXPIRED', 'AUTH_METHOD_LINK_CHALLENGE_ALREADY_USED', 'AUTH_METHOD_LINK_OTP_ATTEMPTS_EXCEEDED'].includes(error?.code);

function providerDisplayName(provider) {
  if (provider === 'FACEBOOK') return 'Facebook';
  if (provider === 'GOOGLE') return 'Google';
  return 'mạng xã hội';
}

export function getAuthProviderErrorMessage(error, provider) {
  // Hai mã lỗi cũ/mới cùng được ánh xạ để UI không phụ thuộc phiên bản Backend đang triển khai.
  if (error?.code === 'AUTH_PROVIDER_ALREADY_LINKED' || error?.code === 'PROVIDER_LINKED_TO_ANOTHER_USER') {
    return `Tài khoản ${providerDisplayName(provider)} này đã được liên kết với một tài khoản UniShare khác. Vui lòng dùng tài khoản ${providerDisplayName(provider)} khác.`;
  }
  return MESSAGES[error?.code] ?? 'Không thể hoàn tất thao tác. Vui lòng thử lại.';
}
