import assert from 'node:assert/strict';
import test from 'node:test';
import {
  normalizeFollowUser,
  resolveCurrentFollowState,
  updateFollowStateInLists,
} from './followListState.js';

test('dùng followedByCurrentUser từ API thay vì suy đoán từ danh sách mock', () => {
  const user = normalizeFollowUser({
    userId: 42,
    displayName: 'Sinh viên A',
    followedByCurrentUser: true,
  });

  assert.equal(user.id, 42);
  assert.equal(user.followedByCurrentUser, true);
});

test('cập nhật trạng thái Follow của cùng user ở cả hai tab và hỗ trợ ID khác kiểu', () => {
  const lists = {
    followers: [{ id: 42, followedByCurrentUser: false }],
    following: [{ id: '42', followedByCurrentUser: false }, { id: 99, followedByCurrentUser: true }],
  };
  const next = updateFollowStateInLists(lists, '42', true);

  assert.equal(next.followers[0].followedByCurrentUser, true);
  assert.equal(next.following[0].followedByCurrentUser, true);
  assert.equal(next.following[1].followedByCurrentUser, true);
});

test('trạng thái API truyền rõ ràng được ưu tiên khi global follow snapshot đã cũ', () => {
  const staleFollows = [];

  assert.equal(resolveCurrentFollowState(true, staleFollows, 1, 42), true);
  assert.equal(resolveCurrentFollowState(false, [{ followerId: 1, followingId: 42 }], 1, 42), false);
  assert.equal(resolveCurrentFollowState(undefined, [{ followerId: '1', followingId: '42' }], 1, 42), true);
});
