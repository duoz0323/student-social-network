import test from 'node:test';
import assert from 'node:assert/strict';
import { buildFacebookLoginOptions } from '../src/features/auth/facebook/facebookSdkAdapter.js';
import { getAuthProviderErrorMessage } from '../src/features/auth/utils/authProviderErrorMapper.js';

// Login thông thường giữ hành vi hiện tại, còn thao tác link phải buộc Facebook hiển thị bước xác nhận lại.
test('link Facebook yêu cầu reauthorize nhưng đăng nhập thông thường thì không', () => {
  assert.deepEqual(buildFacebookLoginOptions(), { scope: 'email', return_scopes: true });
  assert.deepEqual(buildFacebookLoginOptions({ requireReauthorization: true }), {
    scope: 'email',
    return_scopes: true,
    auth_type: 'reauthorize',
  });
});

// Thông báo cho người dùng phải gọi đúng tên dịch vụ và không lộ thuật ngữ kỹ thuật Provider.
test('lỗi Facebook đã thuộc tài khoản khác được hiển thị thân thiện', () => {
  const message = getAuthProviderErrorMessage(
    { code: 'AUTH_PROVIDER_ALREADY_LINKED' },
    'FACEBOOK',
  );

  assert.equal(
    message,
    'Tài khoản Facebook này đã được liên kết với một tài khoản UniShare khác. Vui lòng dùng tài khoản Facebook khác.',
  );
  assert.doesNotMatch(message, /provider/i);
});

// Mã lỗi legacy trong API contract vẫn phải có cùng trải nghiệm hiển thị.
test('mã lỗi provider legacy vẫn được ánh xạ theo tên Google', () => {
  const message = getAuthProviderErrorMessage(
    { code: 'PROVIDER_LINKED_TO_ANOTHER_USER' },
    'GOOGLE',
  );

  assert.match(message, /Tài khoản Google/);
  assert.doesNotMatch(message, /provider/i);
});
