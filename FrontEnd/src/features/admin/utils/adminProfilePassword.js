export function validateAdminPasswordDraft(draft) {
  if (!draft.currentPassword || !draft.newPassword || !draft.confirmPassword) {
    return 'Vui lòng nhập đầy đủ thông tin mật khẩu.';
  }
  if (draft.newPassword.length < 8 || draft.newPassword.length > 72
      || !/[a-z]/.test(draft.newPassword) || !/[A-Z]/.test(draft.newPassword)
      || !/\d/.test(draft.newPassword) || !/[^A-Za-z0-9]/.test(draft.newPassword)) {
    return 'Mật khẩu mới phải từ 8–72 ký tự, có chữ hoa, chữ thường, số và ký tự đặc biệt.';
  }
  if (draft.newPassword !== draft.confirmPassword) return 'Xác nhận mật khẩu mới không khớp.';
  if (draft.currentPassword === draft.newPassword) return 'Mật khẩu mới phải khác mật khẩu hiện tại.';
  return '';
}
