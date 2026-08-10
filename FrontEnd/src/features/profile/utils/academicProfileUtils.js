export const MAX_INTERESTS = 10;
export const MIN_ENTRY_YEAR = 1900;

export function shouldSearchAcademic({ kind, parentId, keyword, searchActive, disabled }) {
  return Boolean(
    searchActive
    && !disabled
    && keyword?.trim()
    && (kind === 'school' || parentId),
  );
}

export function createAcademicSelection(profile = {}) {
  return {
    school: profile.school ?? null,
    faculty: profile.faculty ?? null,
    major: profile.major ?? null,
    entryYear: profile.entryYear ?? null,
  };
}

export function selectSchool(current, school) {
  // Đổi trường phải xóa khoa/ngành cũ để không giữ hierarchy không còn hợp lệ.
  return { ...current, school, faculty: null, major: null };
}

export function selectFaculty(current, faculty) {
  return { ...current, faculty, major: null };
}

export function selectMajor(current, major) {
  return { ...current, major };
}

export function createEntryYears(currentYear = new Date().getFullYear()) {
  const safeCurrentYear = Math.max(MIN_ENTRY_YEAR, Number(currentYear));
  return Array.from(
    { length: safeCurrentYear - MIN_ENTRY_YEAR + 1 },
    (_, index) => safeCurrentYear - index,
  );
}

export function toggleInterest(currentIds, interestId, limit = MAX_INTERESTS) {
  const normalizedId = Number(interestId);
  const uniqueIds = [...new Set((currentIds ?? []).map(Number))];
  if (uniqueIds.includes(normalizedId)) {
    return { interestIds: uniqueIds.filter((id) => id !== normalizedId), limitReached: false };
  }
  if (uniqueIds.length >= limit) {
    return { interestIds: uniqueIds, limitReached: true };
  }
  return { interestIds: [...uniqueIds, normalizedId], limitReached: false };
}

export function buildProfileUpdatePayload({
  basic,
  academic,
  interestIds,
  includeAcademic = false,
  includeInterests = false,
}) {
  const payload = {
    displayName: basic.displayName.trim(),
    dateOfBirth: basic.dateOfBirth,
    bio: basic.bio?.trim() || null,
  };

  if (includeAcademic) {
    payload.academic = {
      schoolId: academic.school?.id ?? null,
      facultyId: academic.faculty?.id ?? null,
      majorId: academic.major?.id ?? null,
      entryYear: academic.entryYear ? Number(academic.entryYear) : null,
    };
  }
  if (includeInterests) payload.interestIds = [...new Set((interestIds ?? []).map(Number))];
  return payload;
}

const ACADEMIC_ERROR_MESSAGES = Object.freeze({
  ACADEMIC_SCHOOL_INVALID: 'Trường đã chọn không còn khả dụng. Vui lòng chọn lại.',
  ACADEMIC_FACULTY_INVALID: 'Khoa đã chọn không còn khả dụng. Vui lòng chọn lại.',
  ACADEMIC_MAJOR_INVALID: 'Ngành đã chọn không còn khả dụng. Vui lòng chọn lại.',
  ACADEMIC_FACULTY_SCHOOL_MISMATCH: 'Khoa không thuộc trường đã chọn. Vui lòng chọn lại khoa.',
  ACADEMIC_MAJOR_FACULTY_MISMATCH: 'Ngành không thuộc khoa đã chọn. Vui lòng chọn lại ngành.',
  ACADEMIC_ENTRY_YEAR_INVALID: 'Năm nhập học phải từ 1900 đến năm hiện tại.',
  INTEREST_INVALID: 'Một sở thích đã chọn không còn khả dụng. Vui lòng tải lại danh sách.',
  INTEREST_LIMIT_EXCEEDED: 'Bạn chỉ có thể chọn tối đa 10 sở thích.',
});

export function mapAcademicProfileError(error) {
  return ACADEMIC_ERROR_MESSAGES[error?.code]
    ?? error?.message
    ?? 'Không thể lưu thông tin. Vui lòng thử lại.';
}
