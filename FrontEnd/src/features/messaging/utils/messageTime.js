const PROJECT_TIME_ZONE = 'Asia/Ho_Chi_Minh';
const PROJECT_OFFSET_MILLIS = 7 * 60 * 60 * 1000;
const DAY_MILLIS = 24 * 60 * 60 * 1000;
const EXPLICIT_TIME_ZONE = /(Z|[+-]\d{2}:?\d{2})$/i;

/** LocalDateTime không offset từ Backend/DB được chuẩn hóa là UTC trước khi đổi sang giờ Việt Nam. */
export function parseMessagingTimestamp(value) {
  if (value instanceof Date) return new Date(value.getTime());
  if (typeof value !== 'string' || !value.trim()) return new Date(Number.NaN);
  const normalized = value.trim();
  return new Date(EXPLICIT_TIME_ZONE.test(normalized) ? normalized : `${normalized}Z`);
}

export function messagingTimestampMillis(value) {
  return parseMessagingTimestamp(value).getTime();
}

export function formatMessagingDateTime(value) {
  const date = parseMessagingTimestamp(value);
  if (!Number.isFinite(date.getTime())) return { time: '', date: '', dayMonth: '', dayKey: '' };
  const parts = new Intl.DateTimeFormat('en-CA', {
    timeZone: PROJECT_TIME_ZONE, year: 'numeric', month: '2-digit', day: '2-digit',
  }).formatToParts(date);
  const part = (type) => parts.find((item) => item.type === type)?.value ?? '';
  return {
    time: new Intl.DateTimeFormat('vi-VN', {
      timeZone: PROJECT_TIME_ZONE, hour: '2-digit', minute: '2-digit', hourCycle: 'h23',
    }).format(date),
    // Ghép từ parts để dấu phân cách luôn là “/” trên mọi trình duyệt/ICU.
    dayMonth: `${part('day')}/${part('month')}`,
    date: `${part('day')}/${part('month')}/${part('year')}`,
    dayKey: `${part('year')}-${part('month')}-${part('day')}`,
  };
}

function dayOrdinal(dayKey) {
  const [year, month, day] = dayKey.split('-').map(Number);
  return Date.UTC(year, month - 1, day) / DAY_MILLIS;
}

/** Nhãn tương đối kiểu Messenger, dựa trên ngày hiện tại tại Việt Nam. */
export function formatMessageGroupTimestamp(value, referenceValue = new Date()) {
  const formatted = formatMessagingDateTime(value);
  const reference = formatMessagingDateTime(referenceValue);
  if (!formatted.time || !formatted.dayKey || !reference.dayKey) return '';
  const dayDifference = dayOrdinal(reference.dayKey) - dayOrdinal(formatted.dayKey);
  if (dayDifference === 0) return formatted.time;
  if (dayDifference === 1) return `Hôm qua, ${formatted.time}`;
  const sameYear = formatted.dayKey.slice(0, 4) === reference.dayKey.slice(0, 4);
  return `${sameYear ? formatted.dayMonth : formatted.date}, ${formatted.time}`;
}

/** Thời gian chờ tới đầu ngày kế tiếp ở UTC+7; Việt Nam không áp dụng DST. */
export function millisecondsUntilNextMessagingDay(referenceValue = new Date()) {
  const now = referenceValue instanceof Date ? referenceValue.getTime() : new Date(referenceValue).getTime();
  if (!Number.isFinite(now)) return DAY_MILLIS;
  const projectTime = now + PROJECT_OFFSET_MILLIS;
  const nextDay = (Math.floor(projectTime / DAY_MILLIS) + 1) * DAY_MILLIS;
  return Math.max(1, nextDay - projectTime);
}
