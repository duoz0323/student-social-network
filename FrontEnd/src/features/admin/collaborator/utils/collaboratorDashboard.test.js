import assert from 'node:assert/strict';
import test from 'node:test';
import { buildInteractionSeries, buildTrendChart } from './collaboratorDashboard.js';

test('bổ sung ngày không có tương tác và cộng đúng ba loại tương tác', () => {
  const series = buildInteractionSeries([{ date: '2026-08-14', likes: 2, comments: 3, reposts: 1 }], 3, new Date(2026, 7, 15, 12));
  assert.deepEqual(series.map((point) => [point.date, point.total]), [['2026-08-13', 0], ['2026-08-14', 6], ['2026-08-15', 0]]);
});

test('tạo tọa độ biểu đồ hữu hạn khi dữ liệu chỉ có một điểm', () => {
  const chart = buildTrendChart([{ date: '2026-08-15', total: 10 }]);
  assert.equal(chart.points.length, 1);
  assert.equal(Number.isFinite(chart.points[0].x), true);
  assert.equal(Number.isFinite(chart.points[0].y), true);
  assert.match(chart.areaPath, /Z$/);
});
