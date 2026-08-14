import assert from 'node:assert/strict';
import test from 'node:test';
import { markerAccessibleTitle, zoomToCluster } from './discoveryMapMarkers.js';

// Cluster click chỉ fit bounds và không có callback chọn Location/panel.
test('cluster click zoom theo bounds', () => {
  const bounds = { id: 'cluster-bounds' };
  let received = null;
  zoomToCluster(null, { bounds }, { fitBounds: (value) => { received = value; } });
  assert.equal(received, bounds);
});

test('marker có nhãn truy cập gồm tên và số bài authoritative', () => {
  assert.equal(markerAccessibleTitle({ displayName: 'Thư viện', postCount: 12 }), 'Thư viện, 12 bài viết');
});
