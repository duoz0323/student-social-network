export const NOTIFICATION_UNREAD_COUNT_EVENT = 'unishare:notification-unread-count';

export function publishNotificationUnreadCount(unreadCount) {
  const normalizedCount = Math.max(0, Number(unreadCount) || 0);
  window.dispatchEvent(new CustomEvent(NOTIFICATION_UNREAD_COUNT_EVENT, {
    detail: { unreadCount: normalizedCount },
  }));
}
