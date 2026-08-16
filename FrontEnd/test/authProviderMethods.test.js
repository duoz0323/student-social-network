import test from 'node:test';
import assert from 'node:assert/strict';
import { normalizeAuthProviderMethods } from '../src/features/auth/utils/authProviderNormalizer.js';
import { validatePasswordMethodForm } from '../src/features/auth/utils/passwordMethodValidation.js';

test('Facebook provider email không làm EMAIL thành phương thức đăng nhập', () => {
  const methods = normalizeAuthProviderMethods({ methods: [
    { type: 'EMAIL', linked: false, state: 'NOT_LINKED', canLink: true },
    { type: 'GOOGLE', linked: false, canLink: true },
    { type: 'FACEBOOK', linked: true, verified: true, canUnlink: false },
  ] });

  assert.deepEqual(methods.map(({ type, linked }) => [type, linked]), [
    ['EMAIL', false], ['GOOGLE', false], ['FACEBOOK', true],
  ]);
  assert.equal(methods[0].state, 'NOT_LINKED');
  assert.equal(methods[0].canSetPassword, false);
  assert.equal(methods[2].canUnlink, false);
});

test('Google verified email chưa password chỉ cho Set Password', () => {
  const [email, google] = normalizeAuthProviderMethods({ methods: [
    { type: 'EMAIL', linked: true, verified: true, state: 'VERIFIED_NO_PASSWORD', maskedIdentifier: 's***@gmail.com', canSetPassword: true, canChangePassword: false, localLoginAvailable: false },
    { type: 'GOOGLE', linked: true, verified: true, canUnlink: false },
    { type: 'FACEBOOK', linked: false, canLink: true },
  ] });

  assert.equal(email.state, 'VERIFIED_NO_PASSWORD');
  assert.equal(email.canSetPassword, true);
  assert.equal(email.canChangePassword, false);
  assert.equal(email.localLoginAvailable, false);
  assert.equal(google.canUnlink, false);
});

test('EMAIL READY cho Change Password và cho phép gỡ social khi còn local login', () => {
  const [email, google] = normalizeAuthProviderMethods({ methods: [
    { type: 'EMAIL', linked: true, verified: true, state: 'READY', passwordConfigured: true, localLoginAvailable: true, canChangePassword: true, canUnlink: true },
    { type: 'GOOGLE', linked: true, verified: true, canUnlink: true },
    { type: 'FACEBOOK', linked: false, canLink: true },
  ] });

  assert.equal(email.canSetPassword, false);
  assert.equal(email.canChangePassword, true);
  assert.equal(email.localLoginAvailable, true);
  assert.equal(google.canUnlink, true);
});

test('form đổi mật khẩu chặn mật khẩu mới trùng mật khẩu hiện tại', () => {
  const validation = validatePasswordMethodForm('CHANGE', {
    currentPassword: 'Kaiyoku112@',
    newPassword: 'Kaiyoku112@',
    confirmPassword: 'Kaiyoku112@',
  });

  assert.equal(validation.sameAsCurrent, true);
  assert.equal(validation.confirmationMismatch, false);
  assert.equal(validation.valid, false);
});

test('form đổi mật khẩu chấp nhận mật khẩu mới hợp lệ và khác mật khẩu hiện tại', () => {
  const validation = validatePasswordMethodForm('CHANGE', {
    currentPassword: 'Kaiyoku112@',
    newPassword: 'Different113@',
    confirmPassword: 'Different113@',
  });

  assert.equal(validation.sameAsCurrent, false);
  assert.equal(validation.confirmationMismatch, false);
  assert.equal(validation.valid, true);
});
