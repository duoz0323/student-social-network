import assert from 'node:assert/strict';
import test from 'node:test';
import { normalizeDiscoveryMapBounds } from './discoveryMapBounds.js';

// Bounds V1 phải hợp lệ và không vượt anti-meridian trước khi service được gọi.
test('chuẩn hóa bounds literal và Google Maps bounds', () => {
  const expected = { north: 10.8, south: 10.7, east: 106.7, west: 106.6 };
  assert.deepEqual(normalizeDiscoveryMapBounds(expected), expected);
  assert.deepEqual(normalizeDiscoveryMapBounds({ toJSON: () => expected }), expected);
});

test('từ chối bounds đảo chiều, anti-meridian hoặc ngoài miền tọa độ', () => {
  assert.equal(normalizeDiscoveryMapBounds({ north: 10, south: 11, east: 107, west: 106 }), null);
  assert.equal(normalizeDiscoveryMapBounds({ north: 11, south: 10, east: -170, west: 170 }), null);
  assert.equal(normalizeDiscoveryMapBounds({ north: 91, south: 10, east: 107, west: 106 }), null);
});
