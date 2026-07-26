const API_DATE_TIME_WITHOUT_TIME_ZONE =
  /^\d{4}-\d{2}-\d{2}T\d{2}:\d{2}(?::\d{2}(?:\.\d{1,9})?)?$/;

/**
 * Backend hiện lưu timestamp do MySQL sinh theo UTC nhưng LocalDateTime không chứa offset.
 * Bổ sung ký hiệu UTC để trình duyệt không hiểu nhầm timestamp đó là giờ cục bộ.
 */
export function parseApiDateTime(value) {
  if (value instanceof Date) return value;
  const normalizedValue =
    typeof value === 'string' && API_DATE_TIME_WITHOUT_TIME_ZONE.test(value)
      ? `${value}Z`
      : value;
  return new Date(normalizedValue);
}

export function formatDateTime(value) {
  const date = parseApiDateTime(value);
  if (Number.isNaN(date.getTime())) return '—';
  return new Intl.DateTimeFormat('vi-VN', {
    day: '2-digit',
    month: '2-digit',
    year: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  }).format(date);
}

// Hiển thị thời gian tương đối: "vừa xong", "X phút", "X giờ", "X ngày"
// phù hợp với mockup hiển thị "· 2h", "· 4h" bên cạnh tên tác giả.
export function shortTime(value) {
  const now = new Date();
  const date = parseApiDateTime(value);
  // Dữ liệu thời gian không hợp lệ không được làm crash toàn bộ PostCard.
  if (Number.isNaN(date.getTime())) return '—';
  const diffSeconds = Math.floor((now - date) / 1000);

  if (diffSeconds < 60) return 'vừa xong';
  const diffMinutes = Math.floor(diffSeconds / 60);
  if (diffMinutes < 60) return `${diffMinutes} phút`;
  const diffHours = Math.floor(diffMinutes / 60);
  if (diffHours < 24) return `${diffHours} giờ`;
  const diffDays = Math.floor(diffHours / 24);
  if (diffDays < 30) return `${diffDays} ngày`;
  // Hơn 30 ngày hiển thị ngày/tháng
  return new Intl.DateTimeFormat('vi-VN', { day: '2-digit', month: '2-digit' }).format(date);
}

export function normalizeText(value) {
  return value.trim().toLowerCase();
}

export function formatNumber(num) {
  if (num == null) return '0';
  if (num >= 1000000) return (num / 1000000).toFixed(1).replace('.', ',').replace(',0', '') + 'M';
  if (num >= 1000) return (num / 1000).toFixed(1).replace('.', ',').replace(',0', '') + 'K';
  return num.toString();
}
