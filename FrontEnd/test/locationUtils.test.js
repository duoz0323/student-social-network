import test from 'node:test';
import assert from 'node:assert/strict';
import { googleMapsLocationUrl } from '../src/features/post/locations/locationUtils.js';

test('tạo Google Maps URL bằng Place ID và không dùng GPS trình duyệt', () => {
  const url = new URL(googleMapsLocationUrl({ placeId: 'ChIJ abc', displayName: 'Đại học STU' }));
  assert.equal(url.origin, 'https://www.google.com');
  assert.equal(url.searchParams.get('api'), '1');
  assert.equal(url.searchParams.get('query_place_id'), 'ChIJ abc');
});
