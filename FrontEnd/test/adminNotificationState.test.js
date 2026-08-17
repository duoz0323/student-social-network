import test from 'node:test';
import assert from 'node:assert/strict';
import { applyAdminNotificationEvent, mergeAdminNotifications } from '../src/features/admin/notifications/adminNotificationState.js';

test('realtime duplicate không tạo hai item và dùng unread count authoritative', () => {
  const item = { notificationId: 7, createdAt: '2026-08-17T10:00:00Z', title: 'Report' };
  const first = applyAdminNotificationEvent({ items: [], unreadCount: 0 }, {
    event: 'ADMIN_NOTIFICATION_CREATED', notification: item, unreadCount: 4,
  });
  const second = applyAdminNotificationEvent(first, {
    event: 'ADMIN_NOTIFICATION_CREATED', notification: item, unreadCount: 4,
  });
  assert.equal(second.items.length, 1);
  assert.equal(second.unreadCount, 4);
});

test('REST reconciliation giữ thứ tự createdAt và loại ID trùng', () => {
  const merged = mergeAdminNotifications(
    [{ notificationId: 1, createdAt: '2026-08-17T09:00:00Z' }],
    [{ notificationId: 2, createdAt: '2026-08-17T10:00:00Z' }, { notificationId: 1, createdAt: '2026-08-17T09:00:00Z' }],
  );
  assert.deepEqual(merged.map((item) => item.notificationId), [2, 1]);
});
