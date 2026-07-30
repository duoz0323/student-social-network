export const NOTIFICATION_TYPE_LABELS = Object.freeze({
  FOLLOW: 'đã theo dõi bạn',
  POST_LIKE: 'đã thích bài viết của bạn',
  POST_COMMENT: 'đã bình luận bài viết của bạn',
  COMMENT_REPLY: 'đã trả lời bình luận của bạn',
  REPORT_RESOLVED: 'Báo cáo của bạn đã được chấp nhận',
  REPORT_REJECTED: 'Báo cáo của bạn đã bị từ chối',
  POST_HIDDEN_BY_ADMIN: 'Bài viết của bạn đã bị ẩn',
  POST_RESTORED_BY_ADMIN: 'Bài viết của bạn đã được khôi phục',
  ACCOUNT_BLOCKED: 'Tài khoản của bạn đã bị khóa',
  ACCOUNT_UNBLOCKED: 'Tài khoản của bạn đã được mở khóa',
});

const SYSTEM_NOTIFICATION_TYPES = new Set([
  'REPORT_RESOLVED',
  'REPORT_REJECTED',
  'POST_HIDDEN_BY_ADMIN',
  'POST_RESTORED_BY_ADMIN',
  'ACCOUNT_BLOCKED',
  'ACCOUNT_UNBLOCKED',
]);

export function getNotificationPresentation(notification) {
  const isSystem = SYSTEM_NOTIFICATION_TYPES.has(notification?.type) || !notification?.actor;
  const actorName = notification?.actor?.displayName?.trim() || 'UniShare';
  const label = NOTIFICATION_TYPE_LABELS[notification?.type] || 'Có hoạt động mới';

  return {
    actorName,
    avatarUrl: notification?.actor?.avatarUrl || null,
    message: isSystem ? label : `${actorName} ${label}`,
    isSystem,
  };
}

export function getNotificationTarget(notification) {
  if (notification?.postId) return `/posts/${encodeURIComponent(notification.postId)}`;
  if (notification?.actor?.userId) return `/profile/${encodeURIComponent(notification.actor.userId)}`;
  return null;
}

export function normalizeNotificationPage(page) {
  return {
    content: Array.isArray(page?.content) ? page.content : [],
    page: Number.isInteger(page?.page) ? page.page : 0,
    last: page?.last !== false,
  };
}
