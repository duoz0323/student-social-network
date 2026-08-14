const SCRIPT_ID = 'google-maps-javascript-sdk';
let loaderPromise;

function loadGoogleMapsSdk() {
  if (window.google?.maps?.importLibrary) return Promise.resolve(window.google);
  if (loaderPromise) return loaderPromise;

  const apiKey = import.meta.env.VITE_GOOGLE_MAPS_API_KEY?.trim();
  if (!apiKey) return Promise.reject(new Error('Thiếu VITE_GOOGLE_MAPS_API_KEY.'));

  loaderPromise = new Promise((resolve, reject) => {
    const callbackName = `__googleMapsReady_${Date.now()}`;
    const cleanup = () => delete window[callbackName];
    const fail = () => {
      cleanup();
      document.getElementById(SCRIPT_ID)?.remove();
      loaderPromise = null;
      reject(new Error('Không thể tải Google Maps.'));
    };

    window[callbackName] = () => {
      cleanup();
      if (window.google?.maps?.importLibrary) resolve(window.google);
      else fail();
    };

    const script = document.createElement('script');
    const params = new URLSearchParams({
      key: apiKey,
      callback: callbackName,
      loading: 'async',
      v: 'weekly',
      language: 'vi',
      region: 'VN',
      auth_referrer_policy: 'origin',
    });
    script.id = SCRIPT_ID;
    script.src = `https://maps.googleapis.com/maps/api/js?${params}`;
    script.async = true;
    script.onerror = fail;
    document.head.appendChild(script);
  });
  return loaderPromise;
}

/** Nạp từng thư viện Google Maps từ cùng một SDK singleton để Places và Map không chèn script trùng. */
export async function loadGoogleMapsLibrary(libraryName) {
  const google = await loadGoogleMapsSdk();
  return google.maps.importLibrary(libraryName);
}

/** Cho phép UI thử tải lại sau lỗi mạng/SDK mà không giữ Promise rejection cũ. */
export function resetGoogleMapsLoader() {
  if (window.google?.maps?.importLibrary) return;
  document.getElementById(SCRIPT_ID)?.remove();
  loaderPromise = null;
}

export function loadPlacesLibrary() {
  return loadGoogleMapsLibrary('places');
}
