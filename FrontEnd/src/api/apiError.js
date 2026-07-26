import axios from 'axios';

const DEFAULT_ERROR_CODE = 'API_ERROR';

function extractFieldErrors(data) {
  const candidate = data?.fieldErrors ?? data?.details?.fieldErrors ?? data?.errors;
  if (Array.isArray(candidate)) {
    // Backend trả danh sách { field, message }; Frontend dùng object để tra cứu lỗi theo tên field.
    return Object.fromEntries(candidate
      .filter((item) => item && typeof item.field === 'string')
      .map((item) => [item.field, item.message ?? 'Giá trị không hợp lệ.']));
  }
  return candidate && typeof candidate === 'object' ? candidate : {};
}

function isRetryableStatus(status) {
  return status === 408 || status === 429 || (status !== null && status >= 500);
}

export class ApiError extends Error {
  constructor({ status = null, code = DEFAULT_ERROR_CODE, message, fieldErrors = {}, details = {}, retryAfterSeconds = null, retryable = false, originalError }) {
    super(message);
    this.name = 'ApiError';
    this.status = status;
    this.code = code;
    this.fieldErrors = fieldErrors;
    this.details = details && typeof details === 'object' ? details : {};
    this.retryAfterSeconds = retryAfterSeconds !== null && retryAfterSeconds !== undefined && Number.isFinite(Number(retryAfterSeconds))
      ? Number(retryAfterSeconds)
      : null;
    this.retryable = retryable;

    // Chỉ giữ lỗi gốc trong development để không đưa chi tiết kỹ thuật vào production UI.
    if (import.meta.env.DEV && originalError) this.originalError = originalError;
  }
}

export function isRequestCanceled(error) {
  return axios.isCancel(error)
    || error?.code === 'ERR_CANCELED'
    || error?.name === 'CanceledError'
    || error?.name === 'AbortError';
}

export function normalizeApiError(error) {
  if (error instanceof ApiError) return error;

  if (isRequestCanceled(error)) {
    // AbortController là lifecycle bình thường khi đổi route, không phải lỗi kết nối máy chủ.
    return new ApiError({
      code: 'ERR_CANCELED',
      message: 'Yêu cầu đã được hủy.',
      originalError: error,
    });
  }

  if (!axios.isAxiosError(error)) {
    return new ApiError({
      code: 'CLIENT_ERROR',
      message: 'Không thể xử lý yêu cầu.',
      originalError: error,
    });
  }

  if (error.code === 'ECONNABORTED' || error.code === 'ETIMEDOUT') {
    return new ApiError({
      code: 'REQUEST_TIMEOUT',
      message: 'Yêu cầu mất quá nhiều thời gian. Vui lòng thử lại.',
      retryable: true,
      originalError: error,
    });
  }

  if (!error.response) {
    return new ApiError({
      code: 'NETWORK_ERROR',
      message: 'Không thể kết nối đến máy chủ. Vui lòng kiểm tra đường truyền.',
      retryable: true,
      originalError: error,
    });
  }

  const status = error.response.status;
  const data = error.response.data;
  const safeMessage = typeof data?.message === 'string' && data.message.trim()
    ? data.message
    : status >= 500
      ? 'Máy chủ đang gặp sự cố. Vui lòng thử lại sau.'
      : 'Yêu cầu không thể được xử lý.';

  return new ApiError({
    status,
    code: typeof data?.code === 'string' ? data.code : DEFAULT_ERROR_CODE,
    message: safeMessage,
    fieldErrors: extractFieldErrors(data),
    details: data?.details,
    retryAfterSeconds: data?.details?.retryAfterSeconds ?? error.response.headers?.['retry-after'],
    retryable: isRetryableStatus(status),
    originalError: error,
  });
}
