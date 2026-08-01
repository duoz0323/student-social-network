import assert from 'node:assert/strict';
import test from 'node:test';
import {
  applyCreatedNotificationEvent,
  canUseNotificationRealtime,
  markAllNotificationsReadState,
  markNotificationReadState,
  mergeUniqueNotifications,
  notificationBadgeLabel,
  rememberEventId,
  removeNotificationState,
  shouldPollNotificationCount,
} from './notificationState.js';

function baseState() {
  return {
    notifications: [
      { notificationId: 1, readAt: null },
      { notificationId: 2, readAt: '2026-07-30T10:00:00' },
    ],
    unreadCount: 1,
  };
}

test('NOTIFICATION_CREATED cập nhật badge và không thêm trùng notificationId', () => {
  const event = {
    eventType: 'NOTIFICATION_CREATED',
    notificationId: 3,
    notification: { notificationId: 3, readAt: null },
    unreadCount: 2,
  };
  const first = applyCreatedNotificationEvent(baseState(), event);
  const duplicate = applyCreatedNotificationEvent(first, { ...event, unreadCount: 2 });

  assert.deepEqual(first.notifications.map((item) => item.notificationId), [3, 1, 2]);
  assert.equal(first.unreadCount, 2);
  assert.equal(duplicate.notifications.length, 3);
});

test('eventId trùng bị nhận diện và LRU có giới hạn', () => {
  const eventIds = new Set();
  assert.equal(rememberEventId(eventIds, 'event-1', 2), true);
  assert.equal(rememberEventId(eventIds, 'event-1', 2), false);
  assert.equal(rememberEventId(eventIds, 'event-2', 2), true);
  assert.equal(rememberEventId(eventIds, 'event-3', 2), true);
  assert.deepEqual([...eventIds], ['event-2', 'event-3']);
});

test('mark read, mark all và xóa notification chưa đọc cập nhật unreadCount an toàn', () => {
  const readOne = markNotificationReadState(baseState(), 1, '2026-07-30T10:01:00');
  assert.equal(readOne.unreadCount, 0);
  assert.equal(readOne.notifications[0].readAt, '2026-07-30T10:01:00');

  const readAll = markAllNotificationsReadState(baseState(), '2026-07-30T10:02:00');
  assert.equal(readAll.unreadCount, 0);
  assert.equal(readAll.notifications.every((item) => item.readAt), true);

  const removed = removeNotificationState(baseState(), 1);
  assert.equal(removed.unreadCount, 0);
  assert.deepEqual(removed.notifications.map((item) => item.notificationId), [2]);
});

test('pagination hợp nhất theo thứ tự mà không trả notification trùng', () => {
  const merged = mergeUniqueNotifications(
    [{ notificationId: 1 }, { notificationId: 2 }],
    [{ notificationId: 2 }, { notificationId: 3 }],
  );
  assert.deepEqual(merged.map((item) => item.notificationId), [1, 2, 3]);
});

test('badge cap 99+ và điều kiện realtime/polling đúng trạng thái phiên', () => {
  assert.equal(notificationBadgeLabel(0), '');
  assert.equal(notificationBadgeLabel(99), '99');
  assert.equal(notificationBadgeLabel(100), '99+');

  assert.equal(canUseNotificationRealtime({
    isAuthenticated: true,
    profileCompleted: true,
    authStatus: 'AUTHENTICATED',
  }), true);
  assert.equal(canUseNotificationRealtime({
    isAuthenticated: true,
    profileCompleted: false,
    authStatus: 'AUTHENTICATED',
  }), false);
  assert.equal(canUseNotificationRealtime({
    isAuthenticated: true,
    profileCompleted: true,
    authStatus: 'BLOCKED',
  }), false);

  assert.equal(shouldPollNotificationCount({
    eligible: true,
    socketConnected: false,
    visibilityState: 'visible',
  }), true);
  assert.equal(shouldPollNotificationCount({
    eligible: true,
    socketConnected: true,
    visibilityState: 'visible',
  }), false);
  assert.equal(shouldPollNotificationCount({
    eligible: true,
    socketConnected: false,
    visibilityState: 'hidden',
  }), false);
});
