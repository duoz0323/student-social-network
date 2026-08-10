/**
 * Chuẩn hóa payload Dashboard để biểu đồ và bảng vẫn an toàn khi API chưa có dữ liệu trong một ngày.
 */
export function normalizeDashboardUserEngagement(payload) {
  return {
    fromDate: payload?.fromDate || '',
    toDate: payload?.toDate || '',
    dailyInteractions: Array.isArray(payload?.dailyInteractions)
      ? payload.dailyInteractions.map((item) => ({
        date: item?.date || '',
        interactionCount: toCount(item?.interactionCount),
      }))
      : [],
    featuredUsers: Array.isArray(payload?.featuredUsers)
      ? payload.featuredUsers.map((user) => ({
        userId: user?.userId ?? null,
        displayName: user?.displayName || 'Người dùng',
        avatarUrl: user?.avatarUrl || '',
        postCount: toCount(user?.postCount),
        interactionCount: toCount(user?.interactionCount),
      }))
      : [],
  };
}

/** Dùng múi giờ UTC vì API analytics phân loại activity theo ngày UTC. */
export function formatDashboardDate(value, options = { day: '2-digit', month: '2-digit' }) {
  if (!/^\d{4}-\d{2}-\d{2}$/.test(value || '')) return '—';
  const [year, month, day] = value.split('-');
  // API đã trả ngày UTC nên format trực tiếp để không phụ thuộc timezone hoặc dấu phân cách của trình duyệt.
  return options.year ? `${day}/${month}/${year}` : `${day}/${month}`;
}

export function formatDashboardCount(value) {
  return new Intl.NumberFormat('vi-VN').format(toCount(value));
}

function toCount(value) {
  const number = Number(value);
  return Number.isFinite(number) && number >= 0 ? number : 0;
}
