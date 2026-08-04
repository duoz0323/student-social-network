import test from 'node:test';
import assert from 'node:assert/strict';
import { createPostForm, resolveLocationUpdate, updatePostForm } from '../src/features/post/locations/locationMultipart.js';

const location = {
  placeId: 'ChIJ-test', displayName: 'Trường Đại học', formattedAddress: null,
  latitude: 10.7382456, longitude: 106.6778123,
};

test('create form gửi Location dưới dạng JSON part', async () => {
  const form = createPostForm({ content: ' Bài viết ', location });
  assert.equal(form.get('content'), 'Bài viết');
  assert.deepEqual(JSON.parse(await form.get('location').text()), location);
});

test('update form luôn gửi LocationAction và chỉ gửi payload khi cần', async () => {
  const form = updatePostForm({ content: 'Nội dung', locationAction: 'REPLACE', location });
  assert.equal(form.get('locationAction'), 'REPLACE');
  assert.deepEqual(JSON.parse(await form.get('location').text()), location);
  assert.equal(updatePostForm({ locationAction: 'REMOVE' }).get('location'), null);
});

test('update form vẫn gửi chuỗi rỗng để cho phép gỡ content và hashtag hiện tại', () => {
  const form = updatePostForm({ content: '   ', hashtag: '', locationAction: 'KEEP' });
  assert.equal(form.get('content'), '');
  assert.equal(form.get('hashtag'), '');
});

test('update form gửi keepMediaIds rỗng để phân biệt gỡ toàn bộ với mặc định giữ media', () => {
  const removeAllForm = updatePostForm({ keepMediaIds: [], locationAction: 'KEEP' });
  const keepByDefaultForm = updatePostForm({ locationAction: 'KEEP' });
  assert.deepEqual(removeAllForm.getAll('keepMediaIds'), ['']);
  assert.deepEqual(keepByDefaultForm.getAll('keepMediaIds'), []);
});

test('suy ra đủ ba hành động KEEP, REPLACE và REMOVE', () => {
  assert.deepEqual(resolveLocationUpdate(null, null), { locationAction: 'KEEP', location: null });
  assert.deepEqual(resolveLocationUpdate(location, null), { locationAction: 'REMOVE', location: null });
  assert.deepEqual(resolveLocationUpdate(location, { ...location }), { locationAction: 'KEEP', location: null });
  const replacement = { ...location, placeId: 'ChIJ-new' };
  assert.deepEqual(resolveLocationUpdate(location, replacement), { locationAction: 'REPLACE', location: replacement });
});
