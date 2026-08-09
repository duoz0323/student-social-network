/**
 * Chuẩn hóa hồ sơ đang đăng nhập về một shape dùng chung cho toàn bộ giao diện.
 * API hồ sơ dùng userId/dateOfBirth, còn Context dùng id/birthDate.
 */
export function toCurrentUserProfile(profile, currentUserId) {
  return {
    id: profile?.userId ?? profile?.id ?? currentUserId,
    displayName: profile?.displayName ?? '',
    avatarUrl: profile?.avatarUrl ?? '',
    birthDate: profile?.dateOfBirth ?? profile?.birthDate ?? null,
    bio: profile?.bio ?? '',
    profileCompletedAt: profile?.profileCompletedAt ?? null,
  };
}

/**
 * Cập nhật một phần hồ sơ ngay sau khi API lưu thành công để Feed, composer
 * và bình luận không phải chờ tải lại trang mới nhận diện đúng người dùng.
 */
export function mergeCurrentUserProfile(currentProfile, patch, currentUserId) {
  return {
    ...toCurrentUserProfile(currentProfile, currentUserId),
    ...patch,
    id: patch?.id ?? currentProfile?.id ?? currentUserId,
  };
}

/** Hồ sơ cache chỉ được dùng khi thực sự thuộc tài khoản đang đăng nhập. */
export function isCurrentUserProfile(profile, currentUserId) {
  return profile?.id != null
    && currentUserId != null
    && String(profile.id) === String(currentUserId);
}
