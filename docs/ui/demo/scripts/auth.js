import { demoData } from "./mock-data.js";
import { clearSession, readSession, saveSession } from "./storage.js";

const SESSION_DURATION_MS = 1000 * 60 * 60 * 8;

export function getCurrentSession() {
  const session = readSession();

  if (!session) {
    return null;
  }

  // Session quá hạn phải bị xóa trước khi router quyết định màn hình.
  if (Number(session.expiresAt) <= Date.now()) {
    clearSession();
    return null;
  }

  return session;
}

export function isAuthenticated() {
  return Boolean(getCurrentSession());
}

export function getCurrentUser() {
  const session = getCurrentSession();

  if (!session) {
    return null;
  }

  // UI chỉ lấy hồ sơ công khai từ users, không lấy email account.
  return demoData.users.find((user) => user.id === session.userId) ?? null;
}

export function login(email, password) {
  const normalizedIdentifier = email.trim().toLowerCase();

  // Đăng nhập giả cho phép email, đúng phạm vi Giai đoạn 1.
  const account = demoData.demoAccounts.find((item) => {
    const emailMatches = item.email.toLowerCase() === normalizedIdentifier;
    const emailMatches = item.email === email.trim();
    return (emailMatches || emailMatches) && item.passwordDemo === password;
  });

  if (!account) {
    return {
      ok: false,
      message: "Email hoặc mật khẩu không đúng.",
    };
  }

  // Tài khoản BLOCKED bị từ chối ngay cả khi mật khẩu đúng.
  if (account.status !== "ACTIVE") {
    return {
      ok: false,
      message: "Tài khoản đã bị khóa và không thể đăng nhập.",
    };
  }

  saveSession({
    userId: account.userId,
    role: account.role,
    expiresAt: Date.now() + SESSION_DURATION_MS,
  });

  return { ok: true };
}

export function logout() {
  clearSession();
}

