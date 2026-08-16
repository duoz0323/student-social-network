import assert from 'node:assert/strict';
import test from 'node:test';
import {
  buildAdminProfilePayload,
  getLatestAdultBirthDate,
  validateAdminAvatarFile,
  validateAdminProfileDraft,
} from '../src/features/admin/utils/adminUserProfile.js';
import { validateAdminPasswordDraft } from '../src/features/admin/utils/adminProfilePassword.js';

test('chuẩn hóa payload hồ sơ trước khi admin gửi Backend', () => {
  const draft = { displayName: '  Nguyễn Văn A  ', dateOfBirth: '2001-06-15', bio: '  Giới thiệu  ' };

  assert.equal(validateAdminProfileDraft(draft), true);
  assert.deepEqual(buildAdminProfilePayload(draft), {
    displayName: 'Nguyễn Văn A',
    dateOfBirth: '2001-06-15',
    bio: 'Giới thiệu',
  });
});

test('không cho lưu hồ sơ thiếu tên, ngày sinh hoặc vượt giới hạn bio', () => {
  assert.equal(validateAdminProfileDraft({ displayName: 'A', dateOfBirth: '2001-06-15', bio: '' }), false);
  assert.equal(validateAdminProfileDraft({ displayName: 'Hợp lệ', dateOfBirth: '', bio: '' }), false);
  assert.equal(validateAdminProfileDraft({ displayName: 'Hợp lệ', dateOfBirth: '2001-06-15', bio: 'x'.repeat(501) }), false);
});

test('ngày sinh tối đa đủ 18 tuổi xử lý đúng ngày nhuận', () => {
  assert.equal(getLatestAdultBirthDate(new Date(2028, 1, 29)), '2010-02-28');
});

test('chỉ chấp nhận avatar đúng định dạng và không vượt quá 10 MB', () => {
  assert.equal(validateAdminAvatarFile({ type: 'image/png', size: 1024 }), '');
  assert.match(validateAdminAvatarFile({ type: 'image/gif', size: 1024 }), /JPG/);
  assert.match(validateAdminAvatarFile({ type: 'image/jpeg', size: 10 * 1024 * 1024 + 1 }), /10 MB/);
});

test('kiểm tra mật khẩu quản trị viên trước khi gửi Backend', () => {
  assert.equal(validateAdminPasswordDraft({
    currentPassword: 'Current123!', newPassword: 'NewPassword123!', confirmPassword: 'NewPassword123!',
  }), '');
  assert.match(validateAdminPasswordDraft({
    currentPassword: 'Current123!', newPassword: 'weak', confirmPassword: 'weak',
  }), /8–72/);
  assert.match(validateAdminPasswordDraft({
    currentPassword: 'Current123!', newPassword: 'NewPassword123!', confirmPassword: 'Different123!',
  }), /không khớp/);
});
