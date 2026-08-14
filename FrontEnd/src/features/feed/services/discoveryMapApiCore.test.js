import assert from 'node:assert/strict';
import test from 'node:test';
import { createDiscoveryMapApi } from './discoveryMapApiCore.js';

// Kiểm tra service giữ đúng query, signal và cursor opaque trước khi nối Axios thật.
test('Discovery Map service tạo đúng request marker và Location Posts', async () => {
  const requests = [];
  const signal = { name: 'abort-signal' };
  const api = createDiscoveryMapApi({
    get: async (url, config) => {
      requests.push({ url, config });
      return { payload: { ok: true } };
    },
    unwrap: async (request) => (await request).payload,
  });

  assert.deepEqual(await api.getMapLocations({ north: 11, south: 10, east: 107, west: 106 }, signal), { ok: true });
  assert.deepEqual(await api.getMapLocationPosts({ locationId: '15', limit: 10, cursor: 'opaque+/=' }, signal), { ok: true });
  assert.deepEqual(requests, [
    {
      url: '/api/v1/discovery/map/locations',
      config: { params: { north: 11, south: 10, east: 107, west: 106 }, signal },
    },
    {
      url: '/api/v1/discovery/map/locations/15/posts',
      config: { params: { limit: 10, cursor: 'opaque+/=' }, signal },
    },
  ]);
});

test('Discovery Map service không gửi cursor khi chưa có', async () => {
  let captured;
  const api = createDiscoveryMapApi({
    get: async (url, config) => {
      captured = { url, config };
      return {};
    },
    unwrap: async (request) => request,
  });
  await api.getMapLocationPosts({ locationId: 8, limit: 10 });
  assert.deepEqual(captured.config.params, { limit: 10 });
});
