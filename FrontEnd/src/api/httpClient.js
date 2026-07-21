import axios from 'axios';
import { apiConfig } from './apiConfig.js';
import { normalizeApiError } from './apiError.js';
import { tokenManager } from './tokenManager.js';

const RETRY_MARKER = '_authRetryAttempted';
let refreshPromise = null;
let authHandlers = {
  getAccessToken: () => tokenManager.getAccessToken(),
  refreshAccessToken: null,
  clearAuthentication: () => tokenManager.clearAccessToken(),
  onProfileNotCompleted: null,
  onBlocked: null,
};

export const httpClient = axios.create({
  baseURL: apiConfig.baseURL,
  timeout: apiConfig.timeout,
  withCredentials: apiConfig.withCredentials,
  headers: { Accept: 'application/json' },
});

// Giai đoạn 13C đăng ký session callbacks tại đây mà không tạo phụ thuộc vòng vào Auth Store.
export function configureHttpAuthHandlers(handlers = {}) {
  authHandlers = { ...authHandlers, ...handlers };
}

httpClient.interceptors.request.use((config) => {
  const accessToken = authHandlers.getAccessToken?.();
  if (accessToken && !config.skipAuth && !config.headers.Authorization) {
    config.headers.Authorization = `Bearer ${accessToken}`;
  }

  // Không đặt Content-Type thủ công cho FormData để Axios tự sinh multipart boundary.
  if (config.data !== undefined && !(config.data instanceof FormData) && !config.headers['Content-Type']) {
    config.headers['Content-Type'] = 'application/json';
  }
  return config;
});

httpClient.interceptors.response.use(
  (response) => response,
  async (error) => {
    const request = error.config;
    const backendCode = error.response?.data?.code;
    if (backendCode === 'PROFILE_NOT_COMPLETED') authHandlers.onProfileNotCompleted?.();
    if (backendCode === 'USER_BLOCKED') authHandlers.onBlocked?.();
    const canRefresh = error.response?.status === 401
      && backendCode === 'ACCESS_TOKEN_EXPIRED'
      && request
      && !request.skipAuthRefresh
      && !request[RETRY_MARKER]
      && typeof authHandlers.refreshAccessToken === 'function';

    if (!canRefresh) throw normalizeApiError(error);

    request[RETRY_MARKER] = true;
    try {
      // Mọi request 401 đồng thời chờ cùng một Promise để tránh refresh storm.
      refreshPromise ??= Promise.resolve().then(() => authHandlers.refreshAccessToken());
      const nextAccessToken = await refreshPromise;
      if (!nextAccessToken) throw new Error('Refresh không trả Access Token mới.');

      request.headers.Authorization = `Bearer ${nextAccessToken}`;
      return await httpClient(request);
    } catch (refreshError) {
      const normalizedError = normalizeApiError(refreshError);
      // Mất mạng tạm thời không chứng minh Refresh Token đã hết hạn hoặc bị thu hồi.
      if (normalizedError.status === 401 || normalizedError.code === 'USER_BLOCKED') {
        authHandlers.clearAuthentication?.();
      }
      throw normalizedError;
    } finally {
      refreshPromise = null;
    }
  },
);
