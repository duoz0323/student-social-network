const ADMIN_STATUS_META = Object.freeze({
  ACTIVE: Object.freeze({ label: 'Hoạt động', tone: 'success', dotClassName: 'bg-emerald-500' }),
  BLOCKED: Object.freeze({ label: 'Đã khóa', tone: 'danger', dotClassName: 'bg-red-500' }),
  PUBLISHED: Object.freeze({ label: 'Đã đăng', tone: 'success', dotClassName: 'bg-emerald-500' }),
  HIDDEN: Object.freeze({ label: 'Đã ẩn', tone: 'warning', dotClassName: 'bg-amber-500' }),
  DELETED: Object.freeze({ label: 'Đã xóa', tone: 'danger', dotClassName: 'bg-red-500' }),
});

/** Chỉ Việt hóa trạng thái khi hiển thị; enum Backend vẫn được giữ nguyên trong dữ liệu và request. */
export function getAdminStatusMeta(status) {
  return ADMIN_STATUS_META[status] ?? {
    label: status || 'Không xác định',
    tone: 'neutral',
    dotClassName: 'bg-zinc-400',
  };
}

export function getAdminStatusLabel(status) {
  return getAdminStatusMeta(status).label;
}
