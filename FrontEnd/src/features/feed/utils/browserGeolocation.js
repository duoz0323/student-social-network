const GEOLOCATION_OPTIONS = Object.freeze({
  enableHighAccuracy: false,
  timeout: 10_000,
  maximumAge: 300_000,
});

function geolocationFailure(kind, message) {
  return Object.assign(new Error(message), { kind });
}

export function mapGeolocationError(error) {
  if (error?.code === 1) {
    return geolocationFailure('permission-denied', 'Bạn chưa cấp quyền vị trí cho UniShare. Hãy cho phép trong cài đặt trình duyệt rồi thử lại.');
  }
  if (error?.code === 3) {
    return geolocationFailure('timeout', 'Không thể xác định vị trí trong thời gian cho phép. Vui lòng thử lại.');
  }
  return geolocationFailure('unavailable', 'Thiết bị chưa thể cung cấp vị trí hiện tại. Hãy kiểm tra dịch vụ vị trí rồi thử lại.');
}

/** Chỉ đọc một snapshot vị trí; không watch, lưu trữ hoặc phát tọa độ sang kênh khác. */
export function getCurrentCoordinates(geolocation, { fresh = false } = {}) {
  if (!geolocation?.getCurrentPosition) {
    return Promise.reject(geolocationFailure('unavailable', 'Trình duyệt này không hỗ trợ xác định vị trí.'));
  }

  return new Promise((resolve, reject) => {
    geolocation.getCurrentPosition(
      (position) => {
        const latitude = Number(position?.coords?.latitude);
        const longitude = Number(position?.coords?.longitude);
        if (!Number.isFinite(latitude) || !Number.isFinite(longitude)
          || latitude < -90 || latitude > 90 || longitude < -180 || longitude > 180) {
          reject(mapGeolocationError({ code: 2 }));
          return;
        }
        resolve({ latitude, longitude });
      },
      (error) => reject(mapGeolocationError(error)),
      { ...GEOLOCATION_OPTIONS, maximumAge: fresh ? 0 : GEOLOCATION_OPTIONS.maximumAge },
    );
  });
}

