/** Chỉ map reference allowlist từ Backend; không điều hướng theo URL tùy ý trong payload. */
export function getAdminNotificationPath(notification) {
  const id = notification?.referenceId;
  switch (notification?.referenceType) {
    case 'MODERATION_CASE': return id ? `/admin/reports/${encodeURIComponent(id)}` : '/admin/reports';
    case 'PROFILE_REPORT': return id ? `/admin/profile-reports/${encodeURIComponent(id)}` : '/admin/reports';
    case 'POST': return id ? `/admin/posts/${encodeURIComponent(id)}` : '/admin/posts';
    case 'USER': return '/admin/users';
    case 'HASHTAG': return '/admin/hashtags';
    case 'ADMIN': return '/admin/admins';
    case 'ROLE': return '/admin/permissions';
    case 'MODERATION_SUGGESTION':
      // Thông báo tạo mới thuộc Moderator; kết quả duyệt được gửi trực tiếp cho Cộng tác viên.
      return notification?.type === 'MODERATION_SUGGESTION_CREATED'
        ? (id ? `/admin/moderation-suggestions/${encodeURIComponent(id)}` : '/admin/moderation-suggestions')
        : (id ? `/admin/collaborator/moderation-suggestions?highlight=${encodeURIComponent(id)}` : '/admin/collaborator/moderation-suggestions');
    default: return null;
  }
}
