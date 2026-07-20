const GOOGLE_SDK_URL = 'https://accounts.google.com/gsi/client';
const SCRIPT_ID = 'google-identity-services-sdk';
const LOAD_TIMEOUT_MS = 10_000;
let sdkPromise = null;

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
