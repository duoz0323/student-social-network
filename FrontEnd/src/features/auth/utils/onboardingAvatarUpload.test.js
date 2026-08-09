import assert from 'node:assert/strict';
import test from 'node:test';
import { uploadSelectedOnboardingAvatar } from './onboardingAvatarUpload.js';

test('không gọi API upload khi người dùng bỏ qua avatar ở onboarding', async () => {
  let called = false;
  const result = await uploadSelectedOnboardingAvatar(null, async () => {
    called = true;
    return { avatarUrl: 'https://cdn.example/avatar.png' };
  });

  assert.equal(result, null);
  assert.equal(called, false);
});

test('upload avatar trả URL trước khi onboarding được hoàn tất', async () => {
  const file = { name: 'avatar.png' };
  const result = await uploadSelectedOnboardingAvatar(file, async (receivedFile) => {
    assert.equal(receivedFile, file);
    return { avatarUrl: 'https://cdn.example/avatar.png' };
  });

  assert.equal(result, 'https://cdn.example/avatar.png');
});

test('không cho tiếp tục onboarding khi API không trả URL avatar', async () => {
  await assert.rejects(
    () => uploadSelectedOnboardingAvatar({ name: 'avatar.png' }, async () => ({})),
    /Không thể tải ảnh đại diện lên/,
  );
});
