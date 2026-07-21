import { authApi } from '../../../api/index.js';
import { tokenManager } from '../../../api/tokenManager.js';

let refreshPromise = null;

function buildSession(response, fallbackUser = null) {
  const user = response.user ?? fallbackUser;
  if (!response.accessToken || !response.refreshToken || !user?.id || !user?.role) {
    throw new Error('Phản hồi Auth không đủ dữ liệu để thiết lập phiên.');
  }
  return { user: { id: user.id, role: user.role }, profileCompleted: Boolean(response.profileCompleted), accessToken: response.accessToken, refreshToken: response.refreshToken };
}

function persistSession(session) {
  tokenManager.setAccessToken(session.accessToken);
  tokenManager.setRefreshToken(session.refreshToken);
  tokenManager.setSessionSnapshot({ user: session.user, profileCompleted: session.profileCompleted });
  return session;
}

export const authService = Object.freeze({
  setAuthenticatedSession(response) {
    return persistSession(buildSession(response));
  },

  async loginLocal(payload, signal) {
    return persistSession(buildSession(await authApi.login(payload, signal)));
  },

  async refreshSession(signal) {
    if (refreshPromise) return refreshPromise;
    const refreshToken = tokenManager.getRefreshToken();
    const snapshot = tokenManager.getSessionSnapshot();
    if (!refreshToken || !snapshot?.user) return null;

    // Một Promise duy nhất bảo đảm bootstrap và interceptor không tạo refresh storm.
    refreshPromise = authApi.refreshToken(refreshToken, signal)
      .then((response) => persistSession(buildSession(response, snapshot.user)))
      .finally(() => { refreshPromise = null; });
    return refreshPromise;
  },

  async logout(signal) {
    const refreshToken = tokenManager.getRefreshToken();
    try {
      if (refreshToken) await authApi.logout(refreshToken, signal);
    } finally {
      // Local session luôn bị xóa, kể cả Backend mất kết nối hoặc token hết hạn.
      tokenManager.clearSession();
    }
  },

  clearSession: () => tokenManager.clearSession(),
  getStoredSession: () => tokenManager.getSessionSnapshot(),
});
