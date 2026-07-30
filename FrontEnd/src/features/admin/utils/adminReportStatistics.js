import { parseApiDateTime } from '../../../utils/formatters.js';

const WEEKDAY_LABELS = ['T2', 'T3', 'T4', 'T5', 'T6', 'T7', 'CN'];

export function getCurrentWeekRange(now = new Date()) {
  const start = new Date(now);
  start.setHours(0, 0, 0, 0);
  const daysFromMonday = start.getDay() === 0 ? 6 : start.getDay() - 1;
  start.setDate(start.getDate() - daysFromMonday);

  const end = new Date(start);
  end.setDate(end.getDate() + 7);
  return { start, end };
}

export function createCurrentWeekReportTrend(reports, now = new Date()) {
  const { start, end } = getCurrentWeekRange(now);
  const currentDayKey = toLocalDateKey(now);
  const days = WEEKDAY_LABELS.map((label, index) => {
    const date = new Date(start);
    date.setDate(start.getDate() + index);
    return {
      label,
      date,
      dateKey: toLocalDateKey(date),
      count: 0,
      isCurrentDay: toLocalDateKey(date) === currentDayKey,
    };
  });
  const dayByKey = new Map(days.map((day) => [day.dateKey, day]));

  reports.forEach((report) => {
    const createdAt = parseApiDateTime(report?.createdAt);
    if (Number.isNaN(createdAt.getTime()) || createdAt < start || createdAt >= end) return;
    const day = dayByKey.get(toLocalDateKey(createdAt));
    if (day) day.count += 1;
  });

  return days;
}

export function isBeforeCurrentWeek(value, now = new Date()) {
  const date = parseApiDateTime(value);
  if (Number.isNaN(date.getTime())) return false;
  return date < getCurrentWeekRange(now).start;
}

export function formatWeekRange(days) {
  if (!days.length) return '';
  return `${formatDayMonth(days[0].date)} - ${formatDayMonth(days.at(-1).date)}`;
}

function toLocalDateKey(date) {
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, '0');
  const day = String(date.getDate()).padStart(2, '0');
  return `${year}-${month}-${day}`;
}

function formatDayMonth(date) {
  return `${String(date.getDate()).padStart(2, '0')}/${String(date.getMonth() + 1).padStart(2, '0')}`;
}
