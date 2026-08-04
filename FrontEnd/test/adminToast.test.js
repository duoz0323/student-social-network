import assert from 'node:assert/strict';
import test from 'node:test';
import { ADMIN_TOAST_DURATION, createAdminToast } from '../src/features/admin/utils/adminToast.js';

test('createAdminToast chuẩn hóa thông báo thành công mặc định', () => {
  assert.deepEqual(createAdminToast('  Cập nhật thành công!  '), {
    message: 'Cập nhật thành công!',
    type: 'success',
    duration: ADMIN_TOAST_DURATION,
  });
});

test('createAdminToast giữ loại lỗi và thời gian hiển thị hợp lệ', () => {
  assert.deepEqual(createAdminToast('Không thể cập nhật.', { type: 'error', duration: 0 }), {
    message: 'Không thể cập nhật.',
    type: 'error',
    duration: 0,
  });
});

test('createAdminToast dùng giá trị an toàn khi đầu vào không hợp lệ', () => {
  assert.deepEqual(createAdminToast('', { type: 'warning', duration: -1 }), {
    message: 'Đã hoàn tất thao tác.',
    type: 'success',
    duration: ADMIN_TOAST_DURATION,
  });
});
