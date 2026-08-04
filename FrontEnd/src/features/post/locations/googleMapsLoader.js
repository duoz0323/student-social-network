let loaderPromise;

/** Nạp Google Maps JavaScript API đúng một lần và chỉ đọc khóa từ môi trường Vite. */
export function loadPlacesLibrary() {
  if (window.google?.maps?.importLibrary) return window.google.maps.importLibrary('places');
  if (loaderPromise) return loaderPromise;

  const apiKey = import.meta.env.VITE_GOOGLE_MAPS_API_KEY;
  if (!apiKey) return Promise.reject(new Error('Thiếu VITE_GOOGLE_MAPS_API_KEY.'));

  loaderPromise = new Promise((resolve, reject) => {
    const callbackName = `__googleMapsReady_${Date.now()}`;
    window[callbackName] = async () => {
      try {
        resolve(await window.google.maps.importLibrary('places'));
      } catch (error) {
        reject(error);
      } finally {
        delete window[callbackName];
      }
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
    script.src = `https://maps.googleapis.com/maps/api/js?${params}`;
    script.async = true;
    script.onerror = () => reject(new Error('Không thể tải Google Places.'));
    document.head.appendChild(script);
  });
  return loaderPromise;
}
