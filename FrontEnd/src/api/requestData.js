import { normalizeApiError } from './apiError.js';

// Chuẩn hóa một lần lớp ApiResponse của Backend để các service chỉ làm việc với payload nghiệp vụ.
export async function requestData(request) {
  try {
    const response = await request;
    return response.data?.data ?? response.data;
  } catch (error) {
    throw normalizeApiError(error);
  }
}

export function compactParams(params = {}) {
  return Object.fromEntries(
    Object.entries(params).filter(([, value]) => value !== undefined && value !== null && value !== ''),
  );
}

// Payload Auth giữ chuỗi rỗng để Backend trả validation đúng field; chỉ bỏ thuộc tính chưa được cung cấp.
export function withoutUndefined(payload = {}) {
  return Object.fromEntries(
    Object.entries(payload).filter(([, value]) => value !== undefined),
  );
}
