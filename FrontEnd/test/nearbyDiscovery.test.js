import test from 'node:test';
import assert from 'node:assert/strict';
import { getCurrentCoordinates } from '../src/features/feed/utils/browserGeolocation.js';
import {
  canLoadMoreNearby,
  formatNearbyDistance,
  nearbyDiscoveryReducer,
  nearbyInitialState,
  shouldRequestNearbyLocation,
} from '../src/features/feed/utils/nearbyDiscoveryState.js';

function readyState(overrides = {}) {
  return {
    ...nearbyInitialState,
    phase: 'success',
    coordinates: { latitude: 10.7, longitude: 106.6 },
    posts: [{ id: 1 }],
    nextCursor: 'opaque-1',
    hasNext: true,
    ...overrides,
  };
}

test('không xin GPS trước khi mở tab Gần bạn và chỉ xin một lần khi tab active', () => {
  assert.equal(shouldRequestNearbyLocation({ active: false, phase: 'initial', inFlight: false }), false);
  assert.equal(shouldRequestNearbyLocation({ active: true, phase: 'initial', inFlight: false }), true);
  assert.equal(shouldRequestNearbyLocation({ active: true, phase: 'initial', inFlight: true }), false);
  assert.equal(shouldRequestNearbyLocation({ active: true, phase: 'success', inFlight: false }), false);
});

test('đọc tọa độ thành công với accuracy vừa phải và refresh không dùng browser cache', async () => {
  const optionsSeen = [];
  const geolocation = {
    getCurrentPosition(success, _failure, options) {
      optionsSeen.push(options);
      success({ coords: { latitude: 10.8231, longitude: 106.6297 } });
    },
  };

  assert.deepEqual(await getCurrentCoordinates(geolocation), { latitude: 10.8231, longitude: 106.6297 });
  await getCurrentCoordinates(geolocation, { fresh: true });
  assert.equal(optionsSeen[0].enableHighAccuracy, false);
  assert.equal(optionsSeen[0].maximumAge, 300_000);
  assert.equal(optionsSeen[1].maximumAge, 0);
});

for (const scenario of [
  { name: 'từ chối quyền', code: 1, kind: 'permission-denied' },
  { name: 'không khả dụng', code: 2, kind: 'unavailable' },
  { name: 'hết thời gian', code: 3, kind: 'timeout' },
]) {
  test(`map đúng lỗi Geolocation: ${scenario.name}`, async () => {
    const geolocation = { getCurrentPosition: (_success, failure) => failure({ code: scenario.code }) };
    await assert.rejects(getCurrentCoordinates(geolocation), (error) => error.kind === scenario.kind);
  });
}

test('trình duyệt không hỗ trợ Geolocation đi vào unavailable', async () => {
  await assert.rejects(getCurrentCoordinates(null), (error) => error.kind === 'unavailable');
});

test('state machine đi qua requesting, location ready, loading và success', () => {
  let state = nearbyDiscoveryReducer(nearbyInitialState, { type: 'LOCATION_REQUESTED' });
  assert.equal(state.phase, 'requesting-location');
  state = nearbyDiscoveryReducer(state, { type: 'LOCATION_READY', coordinates: { latitude: 1, longitude: 2 } });
  assert.equal(state.phase, 'location-ready');
  state = nearbyDiscoveryReducer(state, { type: 'FIRST_PAGE_REQUESTED', requestId: 7 });
  assert.equal(state.phase, 'loading');
  state = nearbyDiscoveryReducer(state, {
    type: 'FIRST_PAGE_SUCCEEDED', requestId: 7, posts: [{ id: 1 }], nextCursor: 'opaque', hasNext: true,
  });
  assert.equal(state.phase, 'success');
  assert.equal(state.paginationPhase, 'idle');
});

test('page đầu rỗng đi vào empty và không tự mở rộng bán kính', () => {
  const loading = nearbyDiscoveryReducer(readyState(), { type: 'FIRST_PAGE_REQUESTED', requestId: 2 });
  const state = nearbyDiscoveryReducer(loading, {
    type: 'FIRST_PAGE_SUCCEEDED', requestId: 2, posts: [], nextCursor: null, hasNext: false,
  });
  assert.equal(state.phase, 'empty');
  assert.equal(state.radiusKm, 5);
  assert.equal(state.paginationPhase, 'end');
});

test('định dạng khoảng cách dưới và trên một kilomet', () => {
  assert.equal(formatNearbyDistance(850), '850 m');
  assert.equal(formatNearbyDistance(1200), '1.2 km');
  assert.equal(formatNearbyDistance(2400), '2.4 km');
  assert.equal(formatNearbyDistance(2000), '2 km');
});

test('đổi radius giữ tọa độ nhưng reset list, cursor và lỗi', () => {
  const state = nearbyDiscoveryReducer(readyState({ loadMoreError: 'Lỗi cũ' }), { type: 'RADIUS_CHANGED', radiusKm: 10 });
  assert.deepEqual(state.coordinates, { latitude: 10.7, longitude: 106.6 });
  assert.equal(state.radiusKm, 10);
  assert.deepEqual(state.posts, []);
  assert.equal(state.nextCursor, null);
  assert.equal(state.loadMoreError, null);
});

test('response radius cũ không thể ghi đè request radius mới', () => {
  let state = nearbyDiscoveryReducer(readyState(), { type: 'FIRST_PAGE_REQUESTED', requestId: 10 });
  state = nearbyDiscoveryReducer(state, { type: 'FIRST_PAGE_REQUESTED', requestId: 11 });
  const stale = nearbyDiscoveryReducer(state, {
    type: 'FIRST_PAGE_SUCCEEDED', requestId: 10, posts: [{ id: 99 }], nextCursor: null, hasNext: false,
  });
  assert.strictEqual(stale, state);
});

test('refresh vị trí giữ radius nhưng xóa tọa độ, list và cursor trong lúc xin lại', () => {
  const state = nearbyDiscoveryReducer(readyState({ radiusKm: 20 }), { type: 'LOCATION_REQUESTED' });
  assert.equal(state.radiusKm, 20);
  assert.equal(state.coordinates, null);
  assert.deepEqual(state.posts, []);
  assert.equal(state.nextCursor, null);
  assert.equal(state.phase, 'requesting-location');
});

test('infinite scroll nối page, khử trùng và đi vào end khi hết cursor', () => {
  let state = nearbyDiscoveryReducer(readyState(), { type: 'LOAD_MORE_REQUESTED', requestId: 3 });
  state = nearbyDiscoveryReducer(state, {
    type: 'LOAD_MORE_SUCCEEDED', requestId: 3, posts: [{ id: 1 }, { id: 2 }], nextCursor: null, hasNext: false,
  });
  assert.deepEqual(state.posts.map((post) => post.id), [1, 2]);
  assert.equal(state.paginationPhase, 'end');
  assert.equal(state.hasNext, false);
});

test('không cho tải thêm đồng thời hoặc sau khi đã hết cursor', () => {
  const base = { active: true, requestInFlight: false, phase: 'success', paginationPhase: 'idle', hasNext: true, nextCursor: 'opaque' };
  assert.equal(canLoadMoreNearby(base), true);
  assert.equal(canLoadMoreNearby({ ...base, requestInFlight: true }), false);
  assert.equal(canLoadMoreNearby({ ...base, paginationPhase: 'loading' }), false);
  assert.equal(canLoadMoreNearby({ ...base, hasNext: false, nextCursor: null }), false);
});

test('lỗi load-more giữ page hiện tại và cho phép retry', () => {
  const loading = nearbyDiscoveryReducer(readyState(), { type: 'LOAD_MORE_REQUESTED', requestId: 4 });
  const state = nearbyDiscoveryReducer(loading, {
    type: 'LOAD_MORE_FAILED', requestId: 4, error: 'Mất mạng', retryable: true,
  });
  assert.deepEqual(state.posts, [{ id: 1 }]);
  assert.equal(state.paginationPhase, 'error');
  assert.equal(state.hasNext, true);
});

test('INVALID_CURSOR dừng phân trang và không tạo vòng retry', () => {
  const loading = nearbyDiscoveryReducer(readyState(), { type: 'LOAD_MORE_REQUESTED', requestId: 5 });
  const state = nearbyDiscoveryReducer(loading, {
    type: 'LOAD_MORE_FAILED', requestId: 5, error: 'Cursor không hợp lệ', retryable: false,
  });
  assert.equal(state.paginationPhase, 'end');
  assert.equal(state.hasNext, false);
  assert.deepEqual(state.posts, [{ id: 1 }]);
});

test('đổi tab hủy request, giữ coordinate runtime và chặn response cũ', () => {
  const loading = nearbyDiscoveryReducer(readyState(), { type: 'FIRST_PAGE_REQUESTED', requestId: 12 });
  const canceled = nearbyDiscoveryReducer(loading, { type: 'REQUEST_CANCELED', requestId: 12 });
  assert.equal(canceled.phase, 'location-ready');
  assert.deepEqual(canceled.coordinates, { latitude: 10.7, longitude: 106.6 });
  const stale = nearbyDiscoveryReducer(canceled, {
    type: 'FIRST_PAGE_SUCCEEDED', requestId: 12, posts: [{ id: 99 }], nextCursor: null, hasNext: false,
  });
  assert.strictEqual(stale, canceled);
});
