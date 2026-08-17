import test from 'node:test';
import assert from 'node:assert/strict';
import {
  getBirthDateValidationMessage,
  getUsernameValidationMessage,
  mapUsernameErrorCode,
  normalizeUsernameInput,
  uploadOnboardingAvatar,
} from '../src/features/auth/components/onboarding/onboardingUtils.js';

test('ngày sinh onboarding báo rõ ngày sai, ngày tương lai và chưa đủ 18 tuổi', () => {
  assert.match(getBirthDateValidationMessage('', '2026-08-17'), /bắt buộc/);
  assert.match(getBirthDateValidationMessage('2026-02-31', '2026-08-17'), /không hợp lệ/);
  assert.match(getBirthDateValidationMessage('2027-01-01', '2026-08-17'), /lớn hơn/);
  assert.match(getBirthDateValidationMessage('2009-08-17', '2026-08-17'), /đủ 18 tuổi/);
  assert.equal(getBirthDateValidationMessage('2008-08-17', '2026-08-17'), '');
});

test('upload avatar onboarding và giữ URL bền vững do Backend trả về', async () => {
  const file = { name: 'avatar.png' };
  const avatarUrl = await uploadOnboardingAvatar(file, async (receivedFile) => {
    assert.equal(receivedFile, file);
    return { avatarUrl: 'https://cdn.example/avatar.png' };
  });

  assert.equal(avatarUrl, 'https://cdn.example/avatar.png');
});

test('không cho onboarding tiếp tục với response upload thiếu avatarUrl', async () => {
  await assert.rejects(
    uploadOnboardingAvatar({ name: 'avatar.png' }, async () => ({})),
    /không trả về URL ảnh đại diện/,
  );
});

test('username state loại @ và chuẩn hóa chữ thường trước khi gọi API', () => {
  assert.equal(normalizeUsernameInput('@DuOz_03'), 'duoz_03');
  assert.equal(normalizeUsernameInput('du@oz'), 'duoz');
});

test('username local validation chặn rỗng, quá ngắn và ký tự ngoài contract', () => {
  assert.match(getUsernameValidationMessage(''), /Vui lòng/);
  assert.match(getUsernameValidationMessage('ab'), /3 đến 30/);
  assert.match(getUsernameValidationMessage('duoz-03'), /chữ thường/);
  assert.equal(getUsernameValidationMessage('duoz_03'), '');
});

test('username Backend errors được ánh xạ vào field onboarding', () => {
  assert.match(mapUsernameErrorCode('USERNAME_REQUIRED'), /Vui lòng/);
  assert.match(mapUsernameErrorCode('USERNAME_INVALID'), /không đúng/);
  assert.match(mapUsernameErrorCode('USERNAME_ALREADY_EXISTS'), /đã tồn tại/);
  assert.match(mapUsernameErrorCode('USERNAME_RESERVED'), /dành riêng/);
  assert.equal(mapUsernameErrorCode('UNRELATED_ERROR'), '');
});
