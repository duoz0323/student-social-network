let accessToken = null;
const REFRESH_TOKEN_KEY = 'unishare.auth.refresh-token';
const SESSION_SNAPSHOT_KEY = 'unishare.auth.session';

function readPersistentValue(key) {
  try {
    const persistedValue = localStorage.getItem(key);
    if (persistedValue !== null) return persistedValue;

    // Chuyển phiên từ bản cũ dùng sessionStorage để người dùng không phải đăng nhập lại sau khi cập nhật.
    const legacyValue = sessionStorage.getItem(key);
    if (legacyValue !== null) {
      localStorage.setItem(key, legacyValue);
      sessionStorage.removeItem(key);
    }
    return legacyValue;
  } catch {
    return null;
  }
}

function writePersistentValue(key, value) {
  try {
    if (value === null) localStorage.removeItem(key);
    else localStorage.setItem(key, value);

    // Dọn dữ liệu bản cũ để chỉ còn một nguồn lưu trạng thái đăng nhập.
    sessionStorage.removeItem(key);
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
    return readPersistentValue(REFRESH_TOKEN_KEY);
  },

  setRefreshToken(token) {
    writePersistentValue(REFRESH_TOKEN_KEY, typeof token === 'string' && token.trim() ? token : null);
  },

  getSessionSnapshot() {
    try {
      const value = readPersistentValue(SESSION_SNAPSHOT_KEY);
      return value ? JSON.parse(value) : null;
    } catch {
      writePersistentValue(SESSION_SNAPSHOT_KEY, null);
      return null;
    }
  },

  setSessionSnapshot(snapshot) {
    writePersistentValue(SESSION_SNAPSHOT_KEY, snapshot ? JSON.stringify(snapshot) : null);
  },

  clearSession() {
    accessToken = null;
    writePersistentValue(REFRESH_TOKEN_KEY, null);
    writePersistentValue(SESSION_SNAPSHOT_KEY, null);
  },
});
