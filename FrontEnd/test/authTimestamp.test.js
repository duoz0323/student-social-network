import test from 'node:test';
import assert from 'node:assert/strict';
import { parseAuthTimestamp, secondsUntilAuthTimestamp } from '../src/features/auth/utils/authTimestamp.js';

// Backend phát LocalDateTime UTC không offset; Frontend không được hiểu nhầm thành giờ máy người dùng.
test('Auth LocalDateTime không offset được parse là UTC', () => {
  assert.equal(
    parseAuthTimestamp('2026-08-18T07:18:00').toISOString(),
    '2026-08-18T07:18:00.000Z',
  );
});

// Deadline vừa tạo phải còn đúng số giây dù trình duyệt chạy ở múi giờ UTC+7.
test('countdown OTP và challenge không hết hạn ngay do lệch múi giờ', () => {
  const now = Date.parse('2026-08-18T07:08:00Z');
  assert.equal(secondsUntilAuthTimestamp('2026-08-18T07:18:00', now), 600);
  assert.equal(secondsUntilAuthTimestamp('2026-08-18T07:23:00', now), 900);
});

// Timestamp đã có offset phải được giữ nguyên để tránh cộng UTC hai lần.
test('Auth timestamp có offset sẵn được giữ nguyên', () => {
  assert.equal(
    parseAuthTimestamp('2026-08-18T14:18:00+07:00').toISOString(),
    '2026-08-18T07:18:00.000Z',
  );
});
