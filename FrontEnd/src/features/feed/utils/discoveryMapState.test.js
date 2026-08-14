import assert from 'node:assert/strict';
import test from 'node:test';
import { discoveryMapInitialState, discoveryMapReducer } from './discoveryMapState.js';

const firstLocation = { locationId: 1, displayName: 'A', postCount: 2 };
const secondLocation = { locationId: 2, displayName: 'B', postCount: 1 };

// Reducer là contract hành vi UI: response cũ không được ghi đè viewport hoặc marker mới.
test('bỏ qua marker response cũ và giữ dirty khi viewport đổi trong lúc request', () => {
  let state = discoveryMapReducer(discoveryMapInitialState, {
    type: 'VIEWPORT_CHANGED',
    viewport: { north: 11, south: 10, east: 107, west: 106 },
  });
  const queryRevision = state.viewportRevision;
  state = discoveryMapReducer(state, { type: 'MARKERS_REQUESTED', requestId: 2 });
  // Cùng viewport không được gửi lặp; pan/zoom trong lúc loading phải mở lại Search để hủy request cũ.
  assert.equal(state.viewportDirty, false);
  state = discoveryMapReducer(state, {
    type: 'MARKERS_SUCCEEDED', requestId: 1, viewportRevision: queryRevision, locations: [firstLocation], truncated: false,
  });
  assert.deepEqual(state.locations, []);

  state = discoveryMapReducer(state, {
    type: 'VIEWPORT_CHANGED', viewport: { north: 12, south: 11, east: 108, west: 107 },
  });
  assert.equal(state.markerPhase, 'loading');
  assert.equal(state.viewportDirty, true);
  state = discoveryMapReducer(state, {
    type: 'MARKERS_SUCCEEDED', requestId: 2, viewportRevision: queryRevision, locations: [firstLocation], truncated: true,
  });
  assert.equal(state.viewportDirty, true);
  assert.equal(state.truncated, true);
});

test('đổi marker reset panel, cursor và bỏ response bài viết cũ', () => {
  let state = discoveryMapReducer(discoveryMapInitialState, { type: 'LOCATION_SELECTED', location: firstLocation });
  state = discoveryMapReducer(state, { type: 'POSTS_REQUESTED', requestId: 10 });
  state = discoveryMapReducer(state, { type: 'LOCATION_SELECTED', location: secondLocation });
  state = discoveryMapReducer(state, { type: 'POSTS_REQUESTED', requestId: 11 });
  state = discoveryMapReducer(state, {
    type: 'POSTS_SUCCEEDED', requestId: 10, posts: [{ id: 100 }], nextCursor: 'old', hasNext: true,
  });
  assert.deepEqual(state.posts, []);

  state = discoveryMapReducer(state, {
    type: 'POSTS_SUCCEEDED', requestId: 11, posts: [{ id: 200 }, { id: 200 }], nextCursor: 'opaque', hasNext: true,
  });
  assert.deepEqual(state.posts.map((post) => post.id), [200]);
  assert.equal(state.nextCursor, 'opaque');
  assert.equal(state.selectedLocation.locationId, 2);
});

test('GPS thành công chỉ đánh dấu viewport dirty và giữ tọa độ trong state runtime', () => {
  const coordinates = { latitude: 10.75, longitude: 106.6 };
  const state = discoveryMapReducer(discoveryMapInitialState, { type: 'GEOLOCATION_SUCCEEDED', coordinates });
  assert.equal(state.viewportDirty, true);
  assert.deepEqual(state.userCoordinates, coordinates);
  assert.match(state.geolocationMessage, /tìm lại/i);
});
