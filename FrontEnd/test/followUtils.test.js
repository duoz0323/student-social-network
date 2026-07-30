import assert from 'node:assert/strict';
import test from 'node:test';

import {
  getUnfollowTargetLabel,
  isFollowingUser,
  resolveFollowingState,
  sameUserId,
} from '../src/utils/followUtils.js';

test('nhãn xác nhận unfollow dùng displayName và chấp nhận userId dạng số', () => {
  assert.equal(getUnfollowTargetLabel({ id: 1002, displayName: 'Nguyễn Văn A' }), 'Nguyễn Văn A');
});

test('nhãn xác nhận không suy diễn thông tin từ userId hoặc email', () => {
  assert.equal(getUnfollowTargetLabel({ id: 1002, email: 'private@example.test' }), 'người dùng này');
});

test('so sánh userId ổn định giữa kiểu số và chuỗi', () => {
  assert.equal(sameUserId(1002, '1002'), true);
  assert.equal(isFollowingUser([{ followerId: '1001', followingId: 1002 }], 1001, '1002'), true);
});

test('trạng thái từ API được ưu tiên để unfollow dù cache dùng chung đang cũ', () => {
  assert.equal(resolveFollowingState(true, [], 1001, 1002), true);
  assert.equal(resolveFollowingState(false, [{ followerId: 1001, followingId: 1002 }], 1001, 1002), false);
});

test('chỉ dùng cache follow khi caller không truyền trạng thái hiện tại', () => {
  const follows = [{ followerId: 1001, followingId: 1002 }];
  assert.equal(resolveFollowingState(undefined, follows, '1001', '1002'), true);
});
