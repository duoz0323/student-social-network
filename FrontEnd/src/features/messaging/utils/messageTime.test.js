import assert from 'node:assert/strict';
import test from 'node:test';
import {
  formatMessageGroupTimestamp,
  formatMessagingDateTime,
  messagingTimestampMillis,
  millisecondsUntilNextMessagingDay,
} from './messageTime.js';

test('LocalDateTime UTC của Backend được đổi sang Asia/Ho_Chi_Minh', () => {
  assert.equal(
    messagingTimestampMillis('2026-08-03T03:15:00'),
    messagingTimestampMillis('2026-08-03T03:15:00Z'),
  );
  assert.equal(formatMessagingDateTime('2026-08-03T03:15:00').time, '10:15');
});

test('timestamp ISO có offset sẵn không bị cộng thêm bảy giờ', () => {
  assert.equal(formatMessagingDateTime('2026-08-03T03:15:00Z').time, '10:15');
});

test('mốc nhóm tin nhắn hiển thị tương đối theo ngày kiểu Messenger', () => {
  const now = '2026-08-04T16:59:00Z';
  assert.equal(formatMessageGroupTimestamp('2026-08-04T10:11:00Z', now), '17:11');
  assert.equal(formatMessageGroupTimestamp('2026-08-03T16:38:00Z', now), 'Hôm qua, 23:38');
  assert.equal(formatMessageGroupTimestamp('2026-07-20T03:15:00Z', now), '20/07, 10:15');
  assert.equal(formatMessageGroupTimestamp('2025-12-31T16:00:00Z', now), '31/12/2025, 23:00');
});

test('tính đúng thời gian tới nửa đêm Việt Nam để tự cập nhật nhãn', () => {
  assert.equal(millisecondsUntilNextMessagingDay('2026-08-04T16:59:00Z'), 60_000);
  assert.equal(millisecondsUntilNextMessagingDay('2026-08-04T17:00:00Z'), 24 * 60 * 60 * 1000);
});
