const EMAIL_PATTERN = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

export function validateLogin(form) {
  const errors = {};
  const email = form.email.trim();
  if (!email) errors.email = 'Vui lòng nhập email.';
  else if (!EMAIL_PATTERN.test(email)) errors.email = 'Email không hợp lệ.';
  if (!form.password) errors.password = 'Vui lòng nhập mật khẩu.';
  else if (form.password.length > 72) errors.password = 'Mật khẩu không được vượt quá 72 ký tự.';
  return errors;
}
