import test from 'node:test';
import assert from 'node:assert/strict';
import {
  applyPostActivity,
  getListMembership,
  isSamePost,
} from './postActivitySync.js';

test('đồng bộ counter và trạng thái viewer cho mọi snapshot của cùng bài viết', () => {
  const post = { id: 15, likeCount: 1, commentCount: 2, repostCount: 3 };
  const updated = applyPostActivity(post, {
    postId: '15',
    viewerUserId: '7',
    likeCount: 4,
    commentCount: 5,
    repostCount: 6,
    likedByCurrentUser: true,
    savedByCurrentUser: true,
    repostedByCurrentUser: true,
  }, 7);

  assert.deepEqual(updated, {
    id: 15,
    likeCount: 4,
    commentCount: 5,
    repostCount: 6,
    likedByCurrentUser: true,
    savedByCurrentUser: true,
    repostedByCurrentUser: true,
  });
});

test('không áp dụng trạng thái riêng tư của viewer khác', () => {
  const post = { id: 15, likedByCurrentUser: false };
  const updated = applyPostActivity(post, {
    postId: 15,
    viewerUserId: 8,
    likedByCurrentUser: true,
    likeCount: 9,
  }, 7);

  assert.equal(updated.likeCount, 9);
  assert.equal(updated.likedByCurrentUser, false);
});

test('xác định đúng bài và membership của danh sách cần tải lại', () => {
  const activity = {
    postId: 15,
    memberships: [
      { cacheKey: 'posts:saved', included: true },
      { cacheKey: 'profile-reposts:7', included: false },
    ],
  };

  assert.equal(isSamePost({ postId: '15' }, activity.postId), true);
  assert.deepEqual(getListMembership(activity, 'posts:saved'), {
    cacheKey: 'posts:saved',
    included: true,
  });
  assert.equal(getListMembership(activity, 'posts:liked'), null);
});
