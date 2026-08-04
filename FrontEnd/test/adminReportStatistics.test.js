import assert from 'node:assert/strict';
import test from 'node:test';
import {
  createCurrentWeekReportTrend,
  formatWeekRange,
  getCurrentWeekRange,
  isBeforeCurrentWeek,
} from '../src/features/admin/utils/adminReportStatistics.js';

test('xác định tuần hiện tại từ Thứ 2 đến trước Thứ 2 tuần kế tiếp', () => {
  const { start, end } = getCurrentWeekRange(new Date('2026-07-31T12:00:00+07:00'));
  assert.equal(start.getDay(), 1);
  assert.equal(start.getHours(), 0);
  assert.equal(end.getTime() - start.getTime(), 7 * 24 * 60 * 60 * 1000);
});

test('đếm báo cáo đúng ngày trong tuần và bỏ dữ liệu ngoài tuần', () => {
  const now = new Date('2026-07-31T12:00:00+07:00');
  const trend = createCurrentWeekReportTrend([
    { createdAt: '2026-07-27T02:00:00Z' },
    { createdAt: '2026-07-31T01:00:00Z' },
    { createdAt: '2026-07-31T06:00:00Z' },
    { createdAt: '2026-07-26T01:00:00Z' },
  ], now);

  assert.deepEqual(trend.map((day) => day.label), ['T2', 'T3', 'T4', 'T5', 'T6', 'T7', 'CN']);
  assert.deepEqual(trend.map((day) => day.count), [1, 0, 0, 0, 2, 0, 0]);
  assert.equal(trend.find((day) => day.isCurrentDay)?.label, 'T6');
  assert.equal(formatWeekRange(trend), '27/07 - 02/08');
});

test('nhận diện thời điểm cũ hơn đầu tuần để dừng tải phân trang', () => {
  const now = new Date('2026-07-31T12:00:00+07:00');
  assert.equal(isBeforeCurrentWeek('2026-07-26T16:59:59Z', now), true);
  assert.equal(isBeforeCurrentWeek('2026-07-26T17:00:00Z', now), false);
});
