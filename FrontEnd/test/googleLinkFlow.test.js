import test from 'node:test';
import assert from 'node:assert/strict';
import { requestGoogleCredential } from '../src/features/auth/google/googleSdkLoader.js';
import { getGoogleErrorMessage } from '../src/features/auth/utils/googleErrorMapper.js';

function createBrowserHarness(onClick) {
  const nativeButton = { click: onClick };
  const container = {
    style: {},
    querySelector: () => nativeButton,
    remove() { this.removed = true; },
  };
  return {
    container,
    documentRef: {
      createElement: () => container,
      body: { appendChild: () => {} },
    },
    windowRef: {
      setTimeout,
      clearTimeout,
    },
  };
}

// Link Google phải dùng nút account chooser chính thức thay vì One Tap dễ bị trình duyệt bỏ qua.
test('link Google render account chooser và trả credential từ callback', async () => {
  let initializedCallback;
  let renderOptions;
  const harness = createBrowserHarness(() => initializedCallback({ credential: 'google-id-token' }));
  const google = { accounts: { id: {
    initialize(options) { initializedCallback = options.callback; },
    renderButton(_container, options) { renderOptions = options; },
  } } };

  const credential = await requestGoogleCredential({
    sdkLoader: async () => google,
    clientId: 'google-client-id',
    documentRef: harness.documentRef,
    windowRef: harness.windowRef,
  });

  assert.equal(credential, 'google-id-token');
  assert.equal(renderOptions.text, 'continue_with');
  assert.equal(harness.container.removed, true);
});

// Nếu SDK không dựng được nút, UI phải nhận mã lỗi có thông báo rõ ràng thay vì fallback chung.
test('link Google báo lỗi thân thiện khi account chooser không khả dụng', async () => {
  const harness = createBrowserHarness(() => {});
  harness.container.querySelector = () => null;
  const google = { accounts: { id: {
    initialize() {},
    renderButton() {},
  } } };

  await assert.rejects(
    requestGoogleCredential({
      sdkLoader: async () => google,
      clientId: 'google-client-id',
      documentRef: harness.documentRef,
      windowRef: harness.windowRef,
    }),
    /GOOGLE_PROMPT_UNAVAILABLE/,
  );
  assert.equal(
    getGoogleErrorMessage(new Error('GOOGLE_PROMPT_UNAVAILABLE')),
    'Không thể mở đăng nhập Google. Vui lòng thử lại.',
  );
});
