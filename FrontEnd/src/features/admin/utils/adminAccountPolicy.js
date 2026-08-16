/** Xác định tài khoản quản trị gốc từ role bất biến do Backend trả về. */
export function isMasterAdmin(admin) {
  return Boolean(admin?.roles?.includes('SUPER_ADMIN'));
}

/** Master Admin chỉ tự quản lý mật khẩu trong Hồ sơ và không nhận thao tác quản trị chéo. */
export function canManageMasterProtectedAccount(admin) {
  return Boolean(admin) && !isMasterAdmin(admin);
}
