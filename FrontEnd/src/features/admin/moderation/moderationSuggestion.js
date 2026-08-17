export const MODERATION_SUGGESTION_REASONS = Object.freeze([
  ['SPAM', 'Spam'],
  ['INAPPROPRIATE_CONTENT', 'Nội dung không phù hợp'],
  ['SCAM_SUSPECTED', 'Nghi ngờ lừa đảo'],
  ['HARASSMENT', 'Quấy rối'],
  ['HARMFUL_CONTENT', 'Nội dung gây hại'],
  ['OTHER', 'Khác'],
]);

const REASON_LABELS = Object.freeze(Object.fromEntries(MODERATION_SUGGESTION_REASONS));
const STATUS_LABELS = Object.freeze({ PENDING: 'Chờ xử lý', ACCEPTED: 'Đã chấp nhận', REJECTED: 'Đã từ chối' });

/** Chuẩn hóa nhãn nghiệp vụ để hai giao diện Collaborator và Moderator luôn đồng bộ. */
export function getSuggestionReasonLabel(reason) {
  return REASON_LABELS[reason] ?? reason ?? '—';
}

export function getSuggestionStatusLabel(status) {
  return STATUS_LABELS[status] ?? status ?? '—';
}

export function getSuggestionStatusClass(status) {
  if (status === 'ACCEPTED') return 'bg-emerald-100 text-emerald-700';
  if (status === 'REJECTED') return 'bg-red-100 text-red-700';
  return 'bg-amber-100 text-amber-700';
}

/** Giữ vai trò nghiệp vụ Cộng tác viên khi đọc response cũ chưa có danh sách role. */
export function getSuggestionActorRoles(actor, fallbackRole) {
  if (actor?.roles?.length) return actor.roles;
  return fallbackRole ? [fallbackRole] : [];
}
