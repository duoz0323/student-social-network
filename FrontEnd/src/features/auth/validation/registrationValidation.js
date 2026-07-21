const EMAIL_PATTERN = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
const PASSWORD_PATTERN = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[^A-Za-z0-9]).{8,72}$/;

export function validateRegistration(form) {
  const errors = {};
  const email = form.email.trim();
  if (!email) errors.email = 'Vui lòng nhập email.';
  else if (!EMAIL_PATTERN.test(email)) errors.email = 'Email không hợp lệ.';
  if (!form.password) errors.password = 'Vui lòng nhập mật khẩu.';
  else if (!PASSWORD_PATTERN.test(form.password)) errors.password = 'Mật khẩu cần 8–72 ký tự, có chữ hoa, chữ thường, số và ký tự đặc biệt.';
  if (form.password !== form.confirmPassword) errors.confirmPassword = 'Mật khẩu xác nhận không khớp.';
  if (!form.acceptTerms) errors.acceptTerms = 'Vui lòng đồng ý điều khoản trước khi đăng ký.';
  return errors;
}

export function validateOtp(code) {
  return /^\d{6}$/.test(code) ? '' : 'Mã OTP phải gồm đúng 6 chữ số.';
}
