let accessToken = null;
const REFRESH_TOKEN_KEY = 'unishare.auth.refresh-token';
const SESSION_SNAPSHOT_KEY = 'unishare.auth.session';

function readSessionValue(key) {
  try {
    return sessionStorage.getItem(key);
  } catch {
    return null;
  }
}

function writeSessionValue(key, value) {
  try {
    if (value === null) sessionStorage.removeItem(key);
    else sessionStorage.setItem(key, value);
  } catch {
    // Trình duyệt có thể chặn storage; phiên memory vẫn tiếp tục trong tab hiện tại.
  }
}

// Access Token chỉ tồn tại trong memory; component không được truy cập storage trực tiếp.
export const tokenManager = Object.freeze({
  getAccessToken() {
    return accessToken;
  },

  setAccessToken(token) {
    accessToken = typeof token === 'string' && token.trim() ? token : null;
  },

  clearAccessToken() {
    accessToken = null;
  },

  hasAccessToken() {
    return Boolean(accessToken);
  },

  getRefreshToken() {
    return readSessionValue(REFRESH_TOKEN_KEY);
  },

  setRefreshToken(token) {
    writeSessionValue(REFRESH_TOKEN_KEY, typeof token === 'string' && token.trim() ? token : null);
  },

  getSessionSnapshot() {
    try {
      const value = readSessionValue(SESSION_SNAPSHOT_KEY);
      return value ? JSON.parse(value) : null;
    } catch {
      writeSessionValue(SESSION_SNAPSHOT_KEY, null);
      return null;
    }
  },

  setSessionSnapshot(snapshot) {
    writeSessionValue(SESSION_SNAPSHOT_KEY, snapshot ? JSON.stringify(snapshot) : null);
  },

  clearSession() {
    accessToken = null;
    writeSessionValue(REFRESH_TOKEN_KEY, null);
    writeSessionValue(SESSION_SNAPSHOT_KEY, null);
  },
});
