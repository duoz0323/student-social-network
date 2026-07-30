import test from 'node:test';
import assert from 'node:assert/strict';
import { formatPostEditCountdown, postEditRemainingSeconds } from '../src/features/post/utils/postEditWindow.js';

test('đếm ngược đủ 15 phút từ timestamp UTC không có offset', () => {
  const publishedAt = '2026-07-29T08:00:00';
  assert.equal(postEditRemainingSeconds(publishedAt, Date.parse('2026-07-29T08:00:00Z')), 900);
  assert.equal(postEditRemainingSeconds(publishedAt, Date.parse('2026-07-29T08:03:00Z')), 720);
  assert.equal(formatPostEditCountdown(720), '12:00');
});

test('countdown về 0 tại deadline và không tạo số âm', () => {
  const publishedAt = '2026-07-29T08:00:00';
  assert.equal(postEditRemainingSeconds(publishedAt, Date.parse('2026-07-29T08:15:00Z')), 0);
  assert.equal(postEditRemainingSeconds(publishedAt, Date.parse('2026-07-29T09:00:00Z')), 0);
  assert.equal(formatPostEditCountdown(0), '00:00');
});

test('timestamp không hợp lệ không làm ẩn nhầm hành động sửa', () => {
  assert.equal(postEditRemainingSeconds('invalid-date'), null);
});

test('sai lệch nhỏ khiến timestamp ở tương lai không làm countdown vượt 15 phút', () => {
  assert.equal(postEditRemainingSeconds('2026-07-29T08:00:03', Date.parse('2026-07-29T08:00:00Z')), 900);
});
