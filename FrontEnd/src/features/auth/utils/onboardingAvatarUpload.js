/**
 * Upload avatar đã chọn trước khi hoàn tất onboarding. Khi có lỗi, caller giữ
 * người dùng tại form để không chuyển sang màn hình thành công với ảnh chỉ là preview cục bộ.
 */
export async function uploadSelectedOnboardingAvatar(avatarFile, uploadAvatar) {
  if (!avatarFile) return null;

  const response = await uploadAvatar(avatarFile);
  if (!response?.avatarUrl) {
    throw new Error('Không thể tải ảnh đại diện lên. Vui lòng thử lại.');
  }
  return response.avatarUrl;
}
