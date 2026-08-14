import assert from 'node:assert/strict';
import test from 'node:test';
import { getCurrentCoordinates, mapGeolocationError } from './browserGeolocation.js';

// Helper không tự chạy; chỉ invocation từ click handler mới gọi getCurrentPosition đúng một lần.
test('geolocation chỉ đọc một snapshot khi được gọi', async () => {
  let calls = 0;
  const geolocation = {
    getCurrentPosition(success, _failure, options) {
      calls += 1;
      assert.equal(options.maximumAge, 0);
      success({ coords: { latitude: 10.75, longitude: 106.6 } });
    },
  };
  assert.equal(calls, 0);
  assert.deepEqual(await getCurrentCoordinates(geolocation, { fresh: true }), { latitude: 10.75, longitude: 106.6 });
  assert.equal(calls, 1);
});

test('map lỗi denied, timeout và trình duyệt không hỗ trợ', async () => {
  assert.equal(mapGeolocationError({ code: 1 }).kind, 'permission-denied');
  assert.equal(mapGeolocationError({ code: 3 }).kind, 'timeout');
  await assert.rejects(() => getCurrentCoordinates(null), (error) => error.kind === 'unavailable');
});
