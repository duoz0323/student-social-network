import assert from 'node:assert/strict';
import test from 'node:test';
import {
  buildAdminProfilePayload,
  getLatestAdultBirthDate,
  validateAdminProfileDraft,
} from '../src/features/admin/utils/adminUserProfile.js';

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
