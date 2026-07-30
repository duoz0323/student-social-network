import { parseApiDateTime } from '../../../utils/formatters.js';

export const POST_EDIT_WINDOW_SECONDS = 15 * 60;

/** Tính số giây còn lại theo timestamp UTC của API; null nghĩa là timestamp không hợp lệ. */
export function postEditRemainingSeconds(publishedAt, nowMs = Date.now()) {
  const publishedDate = parseApiDateTime(publishedAt);
  if (Number.isNaN(publishedDate.getTime())) return null;
  const deadlineMs = publishedDate.getTime() + POST_EDIT_WINDOW_SECONDS * 1000;
  return Math.min(POST_EDIT_WINDOW_SECONDS, Math.max(0, Math.ceil((deadlineMs - nowMs) / 1000)));
}

/** Hiển thị countdown cố định dạng MM:SS cạnh hành động sửa bài. */
export function formatPostEditCountdown(remainingSeconds) {
  const safeSeconds = Math.max(0, Number(remainingSeconds) || 0);
  const minutes = Math.floor(safeSeconds / 60);
  const seconds = safeSeconds % 60;
  return `${String(minutes).padStart(2, '0')}:${String(seconds).padStart(2, '0')}`;
}
