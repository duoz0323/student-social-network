export const ADMIN_TOAST_DURATION = 3000;

const SUPPORTED_TOAST_TYPES = new Set(['success', 'error', 'info']);

export function createAdminToast(message, options = {}) {
  const normalizedMessage = typeof message === 'string' ? message.trim() : '';

  return {
    message: normalizedMessage || 'Đã hoàn tất thao tác.',
    type: SUPPORTED_TOAST_TYPES.has(options.type) ? options.type : 'success',
    duration: Number.isFinite(options.duration) && options.duration >= 0
      ? options.duration
      : ADMIN_TOAST_DURATION,
  };
}
