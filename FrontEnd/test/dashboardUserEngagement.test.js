import test from 'node:test';
import assert from 'node:assert/strict';
import {
  formatDashboardDate,
  normalizeDashboardUserEngagement,
} from '../src/features/admin/utils/dashboardUserEngagement.js';

test('chuẩn hóa dữ liệu Dashboard thiếu trường thành giá trị hiển thị an toàn', () => {
  const result = normalizeDashboardUserEngagement({
    fromDate: '2026-08-01',
    toDate: '2026-08-03',
    dailyInteractions: [{ date: '2026-08-01', interactionCount: '12' }, {}],
    featuredUsers: [{ userId: 7, displayName: 'Mai', postCount: '2', interactionCount: '-1' }],
  });

  assert.deepEqual(result.dailyInteractions, [
    { date: '2026-08-01', interactionCount: 12 },
    { date: '', interactionCount: 0 },
  ]);
  assert.deepEqual(result.featuredUsers[0], {
    userId: 7, displayName: 'Mai', avatarUrl: '', postCount: 2, interactionCount: 0,
  });
});

test('format ngày Dashboard dùng UTC để không lệch ngày theo máy người xem', () => {
  assert.equal(formatDashboardDate('2026-08-03'), '03/08');
  assert.equal(formatDashboardDate('invalid'), '—');
});
