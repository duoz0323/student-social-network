// Tiện ích dùng chung cho các bước onboarding

// Trả về ngày hôm nay dạng YYYY-MM-DD để giới hạn input date
export function todayIsoDate() {
  const today = new Date();
  const year = today.getFullYear();
  const month = String(today.getMonth() + 1).padStart(2, '0');
  const day = String(today.getDate()).padStart(2, '0');
  return `${year}-${month}-${day}`;
}

// Tính tuổi chính xác từ ngày sinh (YYYY-MM-DD)
export function calcAge(dateOfBirth) {
  const [birthYear, birthMonth, birthDay] = dateOfBirth.split('-').map(Number);
  const today = new Date();
  let age = today.getFullYear() - birthYear;
  const monthDifference = today.getMonth() + 1 - birthMonth;
  if (monthDifference < 0 || (monthDifference === 0 && today.getDate() < birthDay)) age--;
  return age;
}

// Chuyển ngày từ contract API YYYY-MM-DD sang định dạng hiển thị DD/MM/YYYY.
export function isoDateToDisplay(value) {
  if (!value) return '';
  const [year, month, day] = value.split('-');
  return year && month && day ? `${day}/${month}/${year}` : '';
}

// Tự chèn dấu phân cách khi người dùng nhập ngày sinh bằng bàn phím số.
export function formatBirthDateInput(value) {
  const digits = value.replace(/\D/g, '').slice(0, 8);
  if (digits.length <= 2) return digits;
  if (digits.length <= 4) return `${digits.slice(0, 2)}/${digits.slice(2)}`;
  return `${digits.slice(0, 2)}/${digits.slice(2, 4)}/${digits.slice(4)}`;
}

// Chỉ trả về ngày theo contract API khi DD/MM/YYYY là một ngày dương lịch hợp lệ.
export function displayDateToIso(value) {
  const match = /^(\d{2})\/(\d{2})\/(\d{4})$/.exec(value);
  if (!match) return '';

  const [, day, month, year] = match;
  const candidate = new Date(Number(year), Number(month) - 1, Number(day));
  const isValid =
    candidate.getFullYear() === Number(year) &&
    candidate.getMonth() === Number(month) - 1 &&
    candidate.getDate() === Number(day);

  return isValid ? `${year}-${month}-${day}` : '';
}

export const inputCls =
  'w-full rounded-xl border px-4 text-base placeholder-zinc-400 outline-none transition focus:border-[var(--auth-input-focus-border)] focus:ring-1 focus:ring-[var(--auth-input-focus-ring)]';

export const inputStyle = {
  background: 'var(--auth-input-bg)',
  borderColor: 'var(--auth-input-border)',
  color: 'var(--app-text)',
};
