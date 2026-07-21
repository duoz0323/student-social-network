const FACEBOOK_SDK_URL = 'https://connect.facebook.net/vi_VN/sdk.js';
const SCRIPT_ID = 'facebook-javascript-sdk';
const SDK_TIMEOUT_MS = 10_000;
const LOGIN_TIMEOUT_MS = 60_000;
let sdkPromise = null;

function withTimeout(register, timeoutMs, timeoutCode) {
  return new Promise((resolve, reject) => {
    let settled = false;
    const finish = (handler, value) => {
      if (settled) return;
      settled = true;
      window.clearTimeout(timeoutId);
      handler(value);
    };
    const timeoutId = window.setTimeout(() => finish(reject, new Error(timeoutCode)), timeoutMs);
    register((value) => finish(resolve, value), (error) => finish(reject, error));
  });
}

export function loadFacebookSdk(appId) {
  if (!appId) return Promise.reject(new Error('FACEBOOK_APP_ID_MISSING'));
  if (window.FB) return Promise.resolve(window.FB);
  if (sdkPromise) return sdkPromise;

  sdkPromise = withTimeout((resolve, reject) => {
    const previousAsyncInit = window.fbAsyncInit;
    window.fbAsyncInit = () => {
      previousAsyncInit?.();
      if (!window.FB) {
        reject(new Error('FACEBOOK_SDK_UNAVAILABLE'));
        return;
      }
      // App ID là cấu hình public; App Secret tuyệt đối không xuất hiện ở Frontend.
      // Đồng bộ phiên bản Graph API đang được Backend production cấu hình.
      window.FB.init({ appId, cookie: false, xfbml: false, version: 'v24.0' });
      resolve(window.FB);
    };

    let script = document.getElementById(SCRIPT_ID);
    if (!script) {
      script = document.createElement('script');
      script.id = SCRIPT_ID;
      script.src = FACEBOOK_SDK_URL;
      script.async = true;
      script.defer = true;
      script.crossOrigin = 'anonymous';
      document.head.appendChild(script);
    }
    script.addEventListener('error', () => {
      script?.remove();
      sdkPromise = null;
      reject(new Error('FACEBOOK_SDK_LOAD_FAILED'));
    }, { once: true });
  }, SDK_TIMEOUT_MS, 'FACEBOOK_SDK_TIMEOUT').catch((error) => {
    sdkPromise = null;
    throw error;
  });

  return sdkPromise;
}

export async function requestFacebookCredential(facebookSdk) {
  const response = await withTimeout((resolve) => {
    // Chỉ yêu cầu email; Backend tự xác minh token và quyết định identity tin cậy.
    facebookSdk.login(resolve, { scope: 'email', return_scopes: true });
  }, LOGIN_TIMEOUT_MS, 'FACEBOOK_LOGIN_TIMEOUT');

  const accessToken = response?.authResponse?.accessToken;
  if (accessToken) return accessToken;
  if (response?.status === 'not_authorized') throw new Error('FACEBOOK_PERMISSION_DENIED');
  throw new Error('FACEBOOK_POPUP_CLOSED');
}
