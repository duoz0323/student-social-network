const MONTH_PATTERN = /^\d{4}-(0[1-9]|1[0-2])$/;

export const USER_GROUPS = Object.freeze([
  { key: 'newActiveUserCount', label: 'Mới hoạt động', shortLabel: 'Mới', color: '#2563eb' },
  { key: 'regularActiveUserCount', label: 'Hoạt động thường xuyên', shortLabel: 'Thường xuyên', color: '#16a34a' },
  { key: 'returningUserCount', label: 'Quay trở lại', shortLabel: 'Quay lại', color: '#7c3aed' },
  { key: 'recentlyInactiveUserCount', label: 'Mới ngừng hoạt động', shortLabel: 'Mới ngừng', color: '#f59e0b' },
  { key: 'eligibleInactiveNotReturnedUserCount', label: 'Đủ ngưỡng, chưa quay lại', shortLabel: 'Chưa quay lại', color: '#ef4444' },
  { key: 'neverActiveUserCount', label: 'Chưa từng hoạt động', shortLabel: 'Chưa dùng', color: '#71717a' },
]);

export function createDefaultAnalyticsFilters(now = new Date()) {
  const toMonth = toMonthValue(now);
  const from = new Date(Date.UTC(now.getUTCFullYear(), now.getUTCMonth() - 5, 1));
  return { fromMonth: toMonthValue(from), toMonth, inactiveDays: 15 };
}

export function validateAnalyticsFilters(filters, now = new Date()) {
  if (!MONTH_PATTERN.test(filters.fromMonth) || !MONTH_PATTERN.test(filters.toMonth)) {
    return 'Vui lòng chọn đầy đủ tháng bắt đầu và tháng kết thúc.';
  }
  const fromIndex = monthIndex(filters.fromMonth);
  const toIndex = monthIndex(filters.toMonth);
  const currentIndex = now.getUTCFullYear() * 12 + now.getUTCMonth();
  if (fromIndex > toIndex) return 'Tháng bắt đầu không được sau tháng kết thúc.';
  if (toIndex > currentIndex) return 'Tháng kết thúc không được nằm trong tương lai.';
  if (toIndex - fromIndex + 1 > 24) return 'Khoảng thống kê không được vượt quá 24 tháng.';
  const inactiveDays = Number(filters.inactiveDays);
  if (!Number.isInteger(inactiveDays) || inactiveDays < 1 || inactiveDays > 365) {
    return 'Số ngày không hoạt động phải là số nguyên từ 1 đến 365.';
  }
  return '';
}

export function normalizeMonthlyAnalytics(payload) {
  const items = Array.isArray(payload?.items) ? payload.items.map(normalizeItem) : [];
  return {
    fromMonth: payload?.fromMonth || '',
    toMonth: payload?.toMonth || '',
    inactiveDays: toCount(payload?.inactiveDays),
    comparisonOperator: payload?.comparisonOperator || 'GREATER_THAN',
    peakReturningMonth: payload?.peakReturningMonth || null,
    peakReturningUserCount: toCount(payload?.peakReturningUserCount),
    peakReturnRateMonth: payload?.peakReturnRateMonth || null,
    peakReturnRate: toNullableNumber(payload?.peakReturnRate),
    items,
  };
}

export function normalizeSummaryAnalytics(payload) {
  return normalizeItem(payload || {});
}

export function formatMonth(month) {
  if (!MONTH_PATTERN.test(month || '')) return '—';
  const [year, value] = month.split('-').map(Number);
  return new Intl.DateTimeFormat('vi-VN', { month: 'long', year: 'numeric', timeZone: 'UTC' })
    .format(new Date(Date.UTC(year, value - 1, 1)));
}

export function formatCount(value) {
  return new Intl.NumberFormat('vi-VN').format(toCount(value));
}

export function formatRate(value) {
  const numericValue = toNullableNumber(value);
  return numericValue === null
    ? 'Chưa có dữ liệu'
    : `${new Intl.NumberFormat('vi-VN', { maximumFractionDigits: 2 }).format(numericValue)}%`;
}

export function hasEligibleUsers(items) {
  return items.some((item) => item.eligibleSystemUserCount > 0);
}

/**
 * Xếp các chỉ số snapshot theo cây ba cấp để giao diện thể hiện đúng quan hệ tổng và thành phần.
 */
export function createMonthlySnapshotRows(summary = {}, inactiveDays = 15) {
  return [
    { key: 'eligible', label: 'Tổng số người dùng đủ điều kiện', count: summary.eligibleSystemUserCount, level: 1 },
    { key: 'active', label: 'Tổng số người dùng đang hoạt động', count: summary.activeUserCount, level: 2, tone: 'active' },
    { key: 'new-active', label: 'Người dùng mới hoạt động', count: summary.newActiveUserCount, level: 3, tone: 'active' },
    { key: 'regular-active', label: 'Người dùng hoạt động thường xuyên', count: summary.regularActiveUserCount, level: 3, tone: 'active' },
    { key: 'returning', label: 'Người dùng quay lại', count: summary.returningUserCount, level: 3, tone: 'active' },
    { key: 'inactive', label: 'Tổng số người dùng không hoạt động', count: summary.inactiveUserCount, level: 2, tone: 'inactive' },
    { key: 'recently-inactive', label: 'Người dùng mới ngừng hoạt động', count: summary.recentlyInactiveUserCount, level: 3, tone: 'inactive' },
    {
      key: 'eligible-inactive',
      label: `Đủ ngưỡng > ${inactiveDays} ngày, chưa quay lại`,
      count: summary.eligibleInactiveNotReturnedUserCount,
      level: 3,
      tone: 'danger',
    },
    { key: 'never-active', label: 'Người dùng chưa từng hoạt động', count: summary.neverActiveUserCount, level: 3, tone: 'inactive' },
  ];
}

function normalizeItem(item) {
  return {
    month: item.month || '',
    evaluationDate: item.evaluationDate || '',
    eligibleSystemUserCount: toCount(item.eligibleSystemUserCount),
    activeUserCount: toCount(item.activeUserCount),
    activeUserRate: toNullableNumber(item.activeUserRate),
    newActiveUserCount: toCount(item.newActiveUserCount),
    regularActiveUserCount: toCount(item.regularActiveUserCount),
    regularActiveRate: toNullableNumber(item.regularActiveRate),
    returningUserCount: toCount(item.returningUserCount),
    recentlyInactiveUserCount: toCount(item.recentlyInactiveUserCount),
    eligibleInactiveUserCount: toCount(item.eligibleInactiveUserCount),
    returningEligibleUserCount: toCount(item.returningEligibleUserCount),
    eligibleInactiveNotReturnedUserCount: toCount(item.eligibleInactiveNotReturnedUserCount),
    returnRate: toNullableNumber(item.returnRate),
    neverActiveUserCount: toCount(item.neverActiveUserCount),
    neverActiveRate: toNullableNumber(item.neverActiveRate),
    inactiveUserCount: toCount(item.inactiveUserCount),
  };
}

function toCount(value) {
  const number = Number(value);
  return Number.isFinite(number) && number >= 0 ? number : 0;
}

function toNullableNumber(value) {
  if (value === null || value === undefined || value === '') return null;
  const number = Number(value);
  return Number.isFinite(number) ? number : null;
}

function monthIndex(value) {
  const [year, month] = value.split('-').map(Number);
  return year * 12 + month - 1;
}

function toMonthValue(date) {
  return `${date.getUTCFullYear()}-${String(date.getUTCMonth() + 1).padStart(2, '0')}`;
}
