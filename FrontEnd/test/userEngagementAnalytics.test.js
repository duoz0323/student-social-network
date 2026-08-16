import test from 'node:test';
import assert from 'node:assert/strict';
import {
  createDefaultAnalyticsFilters,
  createMonthlySnapshotRows,
  formatRate,
  normalizeMonthlyAnalytics,
  validateAnalyticsFilters,
} from '../src/features/admin/utils/userEngagementAnalytics.js';

test('tạo mặc định sáu tháng gần nhất theo UTC', () => {
  const filters = createDefaultAnalyticsFilters(new Date('2026-08-03T05:00:00Z'));
  assert.deepEqual(filters, { fromMonth: '2026-03', toMonth: '2026-08', inactiveDays: 15 });
});

test('snapshot người dùng có đúng cây ba cấp và đặt nhóm không hoạt động trước các chỉ số con', () => {
  const rows = createMonthlySnapshotRows({
    eligibleSystemUserCount: 980,
    activeUserCount: 494,
    newActiveUserCount: 490,
    regularActiveUserCount: 2,
    returningUserCount: 2,
    inactiveUserCount: 486,
    recentlyInactiveUserCount: 1,
    eligibleInactiveNotReturnedUserCount: 5,
    neverActiveUserCount: 480,
  }, 15);

  assert.deepEqual(rows.map(({ key, level }) => ({ key, level })), [
    { key: 'eligible', level: 1 },
    { key: 'active', level: 2 },
    { key: 'new-active', level: 3 },
    { key: 'regular-active', level: 3 },
    { key: 'returning', level: 3 },
    { key: 'inactive', level: 2 },
    { key: 'recently-inactive', level: 3 },
    { key: 'eligible-inactive', level: 3 },
    { key: 'never-active', level: 3 },
  ]);
  assert.equal(rows[7].label, 'Đủ ngưỡng > 15 ngày, chưa quay lại');
});

test('validation chặn khoảng đảo, tương lai, quá 24 tháng và inactiveDays sai', () => {
  const now = new Date('2026-08-03T05:00:00Z');
  assert.match(validateAnalyticsFilters({ fromMonth: '2026-08', toMonth: '2026-07', inactiveDays: 15 }, now), /không được sau/);
  assert.match(validateAnalyticsFilters({ fromMonth: '2026-08', toMonth: '2026-09', inactiveDays: 15 }, now), /tương lai/);
  assert.match(validateAnalyticsFilters({ fromMonth: '2024-08', toMonth: '2026-08', inactiveDays: 15 }, now), /24 tháng/);
  assert.match(validateAnalyticsFilters({ fromMonth: '2026-01', toMonth: '2026-08', inactiveDays: 0 }, now), /1 đến 365/);
  assert.equal(validateAnalyticsFilters({ fromMonth: '2026-01', toMonth: '2026-08', inactiveDays: 15 }, now), '');
});

test('normalize giữ rate null và chuyển count về số an toàn', () => {
  const result = normalizeMonthlyAnalytics({
    peakReturningUserCount: '7',
    peakReturnRate: null,
    items: [{ month: '2026-08', eligibleSystemUserCount: '20', activeUserRate: null }],
  });
  assert.equal(result.peakReturningUserCount, 7);
  assert.equal(result.peakReturnRate, null);
  assert.equal(result.items[0].eligibleSystemUserCount, 20);
  assert.equal(result.items[0].activeUserRate, null);
  assert.equal(formatRate(null), 'Chưa đủ dữ liệu');
});
