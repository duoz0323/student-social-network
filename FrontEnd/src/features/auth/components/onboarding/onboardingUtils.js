// Tiện ích dùng chung cho các bước onboarding

// Trả về ngày hôm nay dạng YYYY-MM-DD để giới hạn input date
export function todayIsoDate() {
  return new Date().toISOString().slice(0, 10);
}

// Tính tuổi chính xác từ ngày sinh (YYYY-MM-DD)
export function calcAge(dateOfBirth) {
  const birth = new Date(dateOfBirth);
  const today = new Date();
  let age = today.getFullYear() - birth.getFullYear();
  const m = today.getMonth() - birth.getMonth();
  if (m < 0 || (m === 0 && today.getDate() < birth.getDate())) age--;
  return age;
}

// Class input dùng chung cho tất cả các trường trong onboarding
export const inputCls =
  'w-full rounded-xl border border-gray-300 bg-white px-4 text-base text-gray-900 ' +
  'placeholder-gray-400 outline-none transition ' +
  'focus:border-violet-600 focus:ring-1 focus:ring-violet-600';
