import test from 'node:test';
import assert from 'node:assert/strict';
import { mediaTypeOfFile, validatePostMediaFiles } from '../src/features/post/utils/postMediaValidation.js';

function file(name, type, size = 1024) {
  return { name, type, size };
}

test('nhận diện đúng loại ảnh và video được hỗ trợ', () => {
  assert.equal(mediaTypeOfFile(file('one.jpg', 'image/jpeg')), 'IMAGE');
  assert.equal(mediaTypeOfFile(file('one.mp4', 'video/mp4')), 'VIDEO');
  assert.equal(mediaTypeOfFile(file('one.gif', 'image/gif')), null);
});

test('validate media mới trên tổng media cũ còn giữ', () => {
  const current = [{ mediaType: 'IMAGE' }, { mediaType: 'IMAGE' }];
  const selected = validatePostMediaFiles(current, [file('three.webp', 'image/webp')]);
  assert.equal(selected[0].mediaType, 'IMAGE');
  assert.throws(
    () => validatePostMediaFiles(current, [file('one.mp4', 'video/mp4'), file('two.webm', 'video/webm')]),
    /tối đa 1 video/,
  );
  assert.throws(
    () => validatePostMediaFiles([...current, { mediaType: 'IMAGE' }, { mediaType: 'IMAGE' }], [file('five.png', 'image/png')]),
    /tối đa 4 media/,
  );
});

test('từ chối định dạng và kích thước media không hợp lệ', () => {
  assert.throws(() => validatePostMediaFiles([], [file('animation.gif', 'image/gif')]), /Chỉ hỗ trợ/);
  assert.throws(
    () => validatePostMediaFiles([], [file('large.png', 'image/png', 10 * 1024 * 1024 + 1)]),
    /10 MB/,
  );
  assert.throws(
    () => validatePostMediaFiles([], [file('large.mp4', 'video/mp4', 100 * 1024 * 1024 + 1)]),
    /100 MB/,
  );
});
