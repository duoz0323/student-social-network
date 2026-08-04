import test from 'node:test';
import assert from 'node:assert/strict';
import { MAX_MESSAGE_IMAGE_BYTES, createImageMessageFormData, validateMessageImages } from './messageImages.js';

function image(name = 'photo.jpg', type = 'image/jpeg', size = 1024) {
  return new File([new Uint8Array(size)], name, { type });
}

test('chấp nhận tối đa 5 ảnh đúng định dạng', () => {
  const result = validateMessageImages([image()], [image('2.png', 'image/png'), image('3.webp', 'image/webp')]);
  assert.equal(result.error, '');
  assert.equal(result.files.length, 3);
});

test('từ chối ảnh vượt số lượng, sai định dạng hoặc quá 10 MB', () => {
  assert.match(validateMessageImages(Array.from({ length: 5 }, () => image()), [image()]).error, /tối đa 5 ảnh/);
  assert.match(validateMessageImages([], [image('bad.gif', 'image/gif')]).error, /JPG/);
  assert.match(validateMessageImages([], [image('large.jpg', 'image/jpeg', MAX_MESSAGE_IMAGE_BYTES + 1)]).error, /10 MB/);
});

test('tạo đúng multipart contract và bỏ caption rỗng', () => {
  const photo = image();
  const payload = createImageMessageFormData({ clientMessageId: 'client-1', content: '  ', images: [photo] });
  assert.equal(payload.get('clientMessageId'), 'client-1');
  assert.equal(payload.has('content'), false);
  assert.equal(payload.getAll('images').length, 1);
});

test('accepts image/jpg alias supplied by some browsers', () => {
  assert.equal(validateMessageImages([], [image('camera.jpg', 'image/jpg')]).error, '');
});
