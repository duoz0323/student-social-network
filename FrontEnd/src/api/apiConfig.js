const DEFAULT_TIMEOUT_MS = 15_000;

function readRequiredUrl(name) {
  const value = import.meta.env[name]?.trim();
  if (!value) {
    throw new Error(`Thiếu biến môi trường công khai ${name}.`);
  }

  try {
    return new URL(value).toString().replace(/\/$/, '');
  } catch {
    throw new Error(`Biến môi trường ${name} phải là một URL hợp lệ.`);
  }
}

function readPositiveInteger(name, fallback) {
  const rawValue = import.meta.env[name];
  if (rawValue === undefined || rawValue === '') return fallback;

  const value = Number(rawValue);
  if (!Number.isInteger(value) || value <= 0) {
    throw new Error(`Biến môi trường ${name} phải là số nguyên dương.`);
  }
  return value;
}

function readBoolean(name, fallback) {
  const rawValue = import.meta.env[name];
  if (rawValue === undefined || rawValue === '') return fallback;
  if (rawValue === 'true') return true;
  if (rawValue === 'false') return false;
  throw new Error(`Biến môi trường ${name} chỉ nhận true hoặc false.`);
}

// Cấu hình được kiểm tra ngay khi module API được nạp để lỗi môi trường xuất hiện sớm.
export const apiConfig = Object.freeze({
  baseURL: readRequiredUrl('VITE_API_BASE_URL'),
  timeout: readPositiveInteger('VITE_API_TIMEOUT_MS', DEFAULT_TIMEOUT_MS),
  withCredentials: readBoolean('VITE_API_WITH_CREDENTIALS', false),
});
