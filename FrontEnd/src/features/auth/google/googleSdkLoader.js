const GOOGLE_SDK_URL = 'https://accounts.google.com/gsi/client';
const SCRIPT_ID = 'google-identity-services-sdk';
const LOAD_TIMEOUT_MS = 10_000;
let sdkPromise = null;
let credentialRequestActive = false;

export function loadGoogleIdentityServices() {
  if (window.google?.accounts?.id) return Promise.resolve(window.google);
  if (sdkPromise) return sdkPromise;

  sdkPromise = new Promise((resolve, reject) => {
    let script = document.getElementById(SCRIPT_ID);
    const timeoutId = window.setTimeout(() => {
      script?.remove();
      sdkPromise = null;
      reject(new Error('GOOGLE_SDK_TIMEOUT'));
    }, LOAD_TIMEOUT_MS);

    function resolveSdk() {
      window.clearTimeout(timeoutId);
      if (window.google?.accounts?.id) resolve(window.google);
      else reject(new Error('GOOGLE_SDK_UNAVAILABLE'));
    }

    function rejectSdk() {
      window.clearTimeout(timeoutId);
      script?.remove();
      sdkPromise = null;
      reject(new Error('GOOGLE_SDK_LOAD_FAILED'));
    }

    if (!script) {
      // Chỉ chèn script chính thức một lần, không sử dụng innerHTML hoặc URL tự dựng.
      script = document.createElement('script');
      script.id = SCRIPT_ID;
      script.src = GOOGLE_SDK_URL;
      script.async = true;
      script.defer = true;
      document.head.appendChild(script);
    }
    script.addEventListener('load', resolveSdk, { once: true });
    script.addEventListener('error', rejectSdk, { once: true });
  });
  return sdkPromise;
}

function createTemporaryGoogleButton(documentRef) {
  const container = documentRef.createElement('div');
  // Nút chỉ tồn tại trong lúc mở account chooser và không làm thay đổi bố cục trang cài đặt.
  Object.assign(container.style, {
    position: 'fixed',
    left: '-10000px',
    top: '0',
    width: '240px',
    height: '44px',
    overflow: 'hidden',
  });
  documentRef.body.appendChild(container);
  return container;
}

// Dùng nút GIS chính thức để mở account chooser; One Tap không phù hợp với thao tác link chủ động.
export async function requestGoogleCredential({
  sdkLoader = loadGoogleIdentityServices,
  clientId = import.meta.env.VITE_GOOGLE_CLIENT_ID?.trim() ?? '',
  documentRef = globalThis.document,
  windowRef = globalThis.window,
} = {}) {
  if (credentialRequestActive) throw new Error('GOOGLE_REQUEST_IN_PROGRESS');
  credentialRequestActive = true;
  let container = null;
  try {
    const google = await sdkLoader();
    return await new Promise((resolve, reject) => {
      let settled = false;
      const finish = (handler, value) => {
        if (settled) return;
        settled = true;
        windowRef.clearTimeout(timeoutId);
        container?.remove();
        handler(value);
      };
      const timeoutId = windowRef.setTimeout(() => finish(reject, new Error('GOOGLE_LOGIN_TIMEOUT')), 60_000);
      container = createTemporaryGoogleButton(documentRef);
      google.accounts.id.initialize({
        client_id: clientId,
        auto_select: false,
        cancel_on_tap_outside: true,
        callback: (response) => response?.credential
          ? finish(resolve, response.credential)
          : finish(reject, new Error('GOOGLE_CREDENTIAL_MISSING')),
      });
      google.accounts.id.renderButton(container, {
        type: 'standard',
        theme: 'outline',
        size: 'large',
        text: 'continue_with',
        shape: 'rectangular',
        width: 240,
      });
      const nativeButton = container.querySelector('[role="button"], button, div[tabindex]');
      if (!nativeButton) {
        finish(reject, new Error('GOOGLE_PROMPT_UNAVAILABLE'));
        return;
      }
      nativeButton.click();
    });
  } finally {
    container?.remove();
    credentialRequestActive = false;
  }
}
