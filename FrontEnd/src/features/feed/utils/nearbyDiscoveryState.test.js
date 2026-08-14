import assert from 'node:assert/strict';
import test from 'node:test';
import { shouldRequestNearbyLocation } from './nearbyDiscoveryState.js';

// Regression: thêm tab Map không được khiến Nearby xin GPS khi Nearby không active.
test('Nearby chỉ xin vị trí khi chính tab Nearby đang active', () => {
  assert.equal(shouldRequestNearbyLocation({ active: false, phase: 'initial', inFlight: false }), false);
  assert.equal(shouldRequestNearbyLocation({ active: true, phase: 'initial', inFlight: false }), true);
  assert.equal(shouldRequestNearbyLocation({ active: true, phase: 'initial', inFlight: true }), false);
});
