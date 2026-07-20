const EMAIL_PATTERN = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
const PHONE_PATTERN = /^\+?\d{9,15}$/;

export function validateLogin(form) {
  const errors = {};
  const identifier = form.identifier.trim();
  if (!identifier) errors.identifier = 'Vui lòng nhập email hoặc số điện thoại.';
  else if (!EMAIL_PATTERN.test(identifier) && !PHONE_PATTERN.test(identifier)) errors.identifier = 'Email hoặc số điện thoại không hợp lệ.';

  // Login không áp dụng lại password policy của đăng ký để không từ chối mật khẩu cũ hợp lệ.
  if (!form.password) errors.password = 'Vui lòng nhập mật khẩu.';
  else if (form.password.length > 72) errors.password = 'Mật khẩu không được vượt quá 72 ký tự.';
  return errors;
}
