/** Merge theo ID để REST reconciliation và realtime không tạo item trùng. */
export function mergeAdminNotifications(current = [], incoming = []) {
  const byId = new Map();
  [...incoming, ...current].forEach((item) => {
    if (item?.notificationId != null && !byId.has(String(item.notificationId))) {
      byId.set(String(item.notificationId), item);
    }
  });
  return [...byId.values()].sort((a, b) => {
    const time = new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime();
    return time || Number(b.notificationId) - Number(a.notificationId);
  });
}

export function applyAdminNotificationEvent(state, envelope) {
  if (envelope?.event !== 'ADMIN_NOTIFICATION_CREATED' || !envelope.notification) return state;
  return {
    ...state,
    items: mergeAdminNotifications(state.items, [envelope.notification]),
    unreadCount: Math.max(0, Number(envelope.unreadCount) || state.unreadCount),
  };
}
