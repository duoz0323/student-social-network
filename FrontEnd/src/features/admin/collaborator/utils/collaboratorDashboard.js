const CHART = Object.freeze({ width: 680, height: 280, top: 18, right: 14, bottom: 34, left: 46 });

/** Bổ sung các ngày không có tương tác bằng 0 để biểu đồ không nối tắt sai dữ liệu. */
export function buildInteractionSeries(points = [], days = 7, now = new Date()) {
  const totalsByDate = new Map(points.map((point) => [point.date, Number(point.likes || 0) + Number(point.comments || 0) + Number(point.reposts || 0)]));
  const end = new Date(now);
  end.setHours(0, 0, 0, 0);
  return Array.from({ length: days }, (_, index) => {
    const date = new Date(end);
    date.setDate(end.getDate() - (days - index - 1));
    const key = `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`;
    return { date: key, total: totalsByDate.get(key) ?? 0 };
  });
}

/** Chuyển chuỗi tương tác thành tọa độ SVG ổn định, kể cả khi chỉ có một điểm. */
export function buildTrendChart(series = []) {
  const maxValue = Math.max(1, ...series.map((point) => point.total));
  const roundedMax = Math.max(4, Math.ceil(maxValue / 4) * 4);
  const plotWidth = CHART.width - CHART.left - CHART.right;
  const plotHeight = CHART.height - CHART.top - CHART.bottom;
  const labelStep = Math.max(1, Math.ceil(series.length / 7));
  const points = series.map((point, index) => {
    const x = CHART.left + (series.length <= 1 ? plotWidth / 2 : (index / (series.length - 1)) * plotWidth);
    const y = CHART.top + plotHeight - (point.total / roundedMax) * plotHeight;
    return { ...point, x, y, label: formatChartDate(point.date, series.length), showLabel: index % labelStep === 0 || index === series.length - 1 };
  });
  const linePath = points.map((point, index) => `${index ? 'L' : 'M'} ${point.x} ${point.y}`).join(' ');
  const baseline = CHART.top + plotHeight;
  const areaPath = points.length ? `${linePath} L ${points.at(-1).x} ${baseline} L ${points[0].x} ${baseline} Z` : '';
  const gridLines = Array.from({ length: 5 }, (_, index) => {
    const value = Math.round((roundedMax * index) / 4);
    return { value, y: CHART.top + plotHeight - (value / roundedMax) * plotHeight };
  });
  return { ...CHART, points, linePath, areaPath, gridLines };
}

function formatChartDate(value, length) {
  const date = new Date(`${value}T00:00:00`);
  if (Number.isNaN(date.getTime())) return value;
  return new Intl.DateTimeFormat('vi-VN', length <= 7 ? { weekday: 'short' } : { day: '2-digit', month: '2-digit' }).format(date);
}
