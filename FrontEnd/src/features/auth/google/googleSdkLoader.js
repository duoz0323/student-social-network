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

// Các dialog liên kết tài khoản chưa có vùng render nút chính thức nên dùng prompt có khóa chống gọi trùng.
export async function requestGoogleCredential() {
  if (credentialRequestActive) throw new Error('GOOGLE_REQUEST_IN_PROGRESS');
  credentialRequestActive = true;
  try {
    const google = await loadGoogleIdentityServices();
    return await new Promise((resolve, reject) => {
      let settled = false;
      const finish = (handler, value) => {
        if (settled) return;
        settled = true;
        window.clearTimeout(timeoutId);
        handler(value);
      };
      const timeoutId = window.setTimeout(() => finish(reject, new Error('GOOGLE_LOGIN_TIMEOUT')), 60_000);
      google.accounts.id.initialize({
        client_id: import.meta.env.VITE_GOOGLE_CLIENT_ID?.trim() ?? '',
        auto_select: false,
        cancel_on_tap_outside: true,
        callback: (response) => response?.credential
          ? finish(resolve, response.credential)
          : finish(reject, new Error('GOOGLE_CREDENTIAL_MISSING')),
      });
      google.accounts.id.prompt((notification) => {
        if (notification?.isNotDisplayed?.() || notification?.isSkippedMoment?.()) {
          finish(reject, new Error('GOOGLE_PROMPT_UNAVAILABLE'));
        }
      });
    });
  } finally {
    credentialRequestActive = false;
  }
}
