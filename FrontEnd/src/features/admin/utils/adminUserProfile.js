export const ADMIN_PROFILE_LIMITS = Object.freeze({
  minimumDisplayNameLength: 2,
  maximumDisplayNameLength: 100,
  maximumBioLength: 500,
  maximumAvatarSizeBytes: 10 * 1024 * 1024,
});

const ALLOWED_AVATAR_TYPES = new Set(['image/jpeg', 'image/png', 'image/webp']);

export function validateAdminAvatarFile(file) {
  if (!file) return 'Vui lòng chọn một ảnh đại diện.';
  if (!ALLOWED_AVATAR_TYPES.has(file.type)) return 'Ảnh phải có định dạng JPG, JPEG, PNG hoặc WEBP.';
  if (file.size > ADMIN_PROFILE_LIMITS.maximumAvatarSizeBytes) return 'Ảnh đại diện không được vượt quá 10 MB.';
  return '';
}

export function validateAdminProfileDraft(draft) {
  const displayNameLength = draft.displayName.trim().length;
  return displayNameLength >= ADMIN_PROFILE_LIMITS.minimumDisplayNameLength
    && displayNameLength <= ADMIN_PROFILE_LIMITS.maximumDisplayNameLength
    && Boolean(draft.dateOfBirth)
    && draft.bio.length <= ADMIN_PROFILE_LIMITS.maximumBioLength;
}

export function buildAdminProfilePayload(draft) {
  return {
    displayName: draft.displayName.trim(),
    dateOfBirth: draft.dateOfBirth,
    bio: draft.bio.trim(),
  };
}

export function getLatestAdultBirthDate(referenceDate = new Date()) {
  const year = referenceDate.getFullYear() - 18;
  const monthIndex = referenceDate.getMonth();
  // Ngày cuối tháng giúp xử lý đúng trường hợp ngày tham chiếu là 29/02.
  const lastDayOfMonth = new Date(year, monthIndex + 1, 0).getDate();
  const day = Math.min(referenceDate.getDate(), lastDayOfMonth);
  return `${year}-${String(monthIndex + 1).padStart(2, '0')}-${String(day).padStart(2, '0')}`;
}
