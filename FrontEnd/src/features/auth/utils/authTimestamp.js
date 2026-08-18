const EXPLICIT_TIME_ZONE = /(Z|[+-]\d{2}:?\d{2})$/i;

/** LocalDateTime không offset từ Auth Backend được chuẩn hóa là UTC theo contract của dự án. */
export function parseAuthTimestamp(value) {
  if (value instanceof Date) return new Date(value.getTime());
  if (typeof value !== 'string' || !value.trim()) return new Date(Number.NaN);
  const normalized = value.trim();
  return new Date(EXPLICIT_TIME_ZONE.test(normalized) ? normalized : `${normalized}Z`);
}

/** Tính countdown an toàn; timestamp thiếu/sai định dạng được xem là không còn hiệu lực. */
export function secondsUntilAuthTimestamp(value, now = Date.now()) {
  const timestamp = parseAuthTimestamp(value).getTime();
  if (!Number.isFinite(timestamp)) return 0;
  return Math.max(0, Math.ceil((timestamp - now) / 1000));
}
