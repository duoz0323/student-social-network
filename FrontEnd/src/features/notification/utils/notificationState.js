export const NOTIFICATION_FIRST_PAGE_SIZE = 20;
export const MAX_PROCESSED_EVENT_IDS = 500;

export function notificationBadgeLabel(unreadCount) {
  if (!Number.isFinite(unreadCount) || unreadCount <= 0) return '';
  return unreadCount > 99 ? '99+' : String(Math.trunc(unreadCount));
}

export function canUseNotificationRealtime(authState) {
  return Boolean(
    authState?.isAuthenticated
    && authState.profileCompleted
    && authState.authStatus !== 'BLOCKED',
  );
}

export function shouldPollNotificationCount({ eligible, socketConnected, visibilityState }) {
  return Boolean(eligible && !socketConnected && visibilityState === 'visible');
}

export function mergeUniqueNotifications(primary = [], secondary = []) {
  const seen = new Set();
  return [...primary, ...secondary].filter((notification) => {
    const id = String(notification?.notificationId ?? '');
    if (!id || seen.has(id)) return false;
    seen.add(id);
    return true;
  });
}

export function applyCreatedNotificationEvent(state, event) {
  if (!event || event.eventType !== 'NOTIFICATION_CREATED' || !event.notification) return state;

  const hasNotification = state.notifications.some((item) =>
    String(item.notificationId) === String(event.notificationId));
  const nextUnreadCount = Number.isSafeInteger(event.unreadCount) && event.unreadCount >= 0
    ? event.unreadCount
    : state.unreadCount;

  return {
    ...state,
    notifications: hasNotification
      ? state.notifications
      : [event.notification, ...state.notifications],
    unreadCount: nextUnreadCount,
  };
}

export function markNotificationReadState(state, notificationId, readAt) {
  let changedUnread = false;
  const notifications = state.notifications.map((item) => {
    if (String(item.notificationId) !== String(notificationId) || item.readAt) return item;
    changedUnread = true;
    return { ...item, readAt };
  });
  return {
    ...state,
    notifications,
    unreadCount: changedUnread ? Math.max(0, state.unreadCount - 1) : state.unreadCount,
  };
}

export function markAllNotificationsReadState(state, readAt) {
  return {
    ...state,
    notifications: state.notifications.map((item) => ({
      ...item,
      readAt: item.readAt || readAt,
    })),
    unreadCount: 0,
  };
}

export function removeNotificationState(state, notificationId) {
  const removed = state.notifications.find((item) =>
    String(item.notificationId) === String(notificationId));
  return {
    ...state,
    notifications: state.notifications.filter((item) =>
      String(item.notificationId) !== String(notificationId)),
    unreadCount: removed && !removed.readAt
      ? Math.max(0, state.unreadCount - 1)
      : state.unreadCount,
  };
}

export function rememberEventId(eventIds, eventId, maxSize = MAX_PROCESSED_EVENT_IDS) {
  if (!eventId || eventIds.has(eventId)) return false;
  eventIds.add(eventId);
  while (eventIds.size > maxSize) {
    eventIds.delete(eventIds.values().next().value);
  }
  return true;
}
