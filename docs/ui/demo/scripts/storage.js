const SESSION_KEY = "unishare.demo.session";

// Lưu đúng ba trường được phép để tránh đưa dữ liệu nhạy cảm vào localStorage.
export function saveSession({ userId, role, expiresAt }) {
  localStorage.setItem(SESSION_KEY, JSON.stringify({ userId, role, expiresAt }));
}

export function readSession() {
  const rawSession = localStorage.getItem(SESSION_KEY);

  // Không có session nghĩa là người dùng chưa đăng nhập trong demo hiện tại.
  if (!rawSession) {
    return null;
  }

  try {
    return JSON.parse(rawSession);
  } catch {
    // Session hỏng sẽ bị xóa để route guard không dùng dữ liệu sai.
    clearSession();
    return null;
  }
}

export function clearSession() {
  localStorage.removeItem(SESSION_KEY);
}
