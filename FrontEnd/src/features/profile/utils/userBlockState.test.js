import assert from 'node:assert/strict';
import test from 'node:test';
import { postListCache } from '../../post/hooks/postListCache.js';
import {
  invalidateUserBlockCaches,
  removeBlockedUserFromState,
} from './userBlockState.js';

test('Block xóa dữ liệu liên quan nhưng giữ nguyên user không liên quan', () => {
  const state = {
    users: [{ id: 1 }, { id: 2, email: 'hidden@example.com' }, { id: 3 }],
    posts: [{ id: 10, authorId: 2 }, { id: 11, authorId: 3 }],
    follows: [
      { followerId: 1, followingId: 2 },
      { followerId: 2, followingId: 1 },
      { followerId: 1, followingId: 3 },
    ],
    likes: [{ userId: 1, postId: 10 }, { userId: 1, postId: 11 }],
    savedPosts: [{ userId: 1, postId: 10 }, { userId: 1, postId: 11 }],
    comments: [
      { id: 20, authorId: 2, postId: 11 },
      { id: 22, authorId: 3, postId: 11, parentCommentId: 20 },
      { id: 21, authorId: 3, postId: 11 },
      { id: 23, authorId: 2, postId: 11, parentCommentId: 21 },
    ],
  };

  const next = removeBlockedUserFromState(state, 1, 2);

  assert.deepEqual(next.users, [{ id: 1 }, { id: 3 }]);
  assert.deepEqual(next.posts, [{ id: 11, authorId: 3 }]);
  assert.deepEqual(next.follows, [{ followerId: 1, followingId: 3 }]);
  assert.deepEqual(next.likes, [{ userId: 1, postId: 11 }]);
  assert.deepEqual(next.savedPosts, [{ userId: 1, postId: 11 }]);
  assert.deepEqual(next.comments, [{ id: 21, authorId: 3, postId: 11 }]);
});

test('Block chỉ xóa cache người dùng, không xóa cache Admin', () => {
  postListCache.clear();
  postListCache.set('feed:following', {});
  postListCache.set('liked-posts', {});
  postListCache.set('admin:posts', {});

  invalidateUserBlockCaches();

  assert.equal(postListCache.has('feed:following'), false);
  assert.equal(postListCache.has('liked-posts'), false);
  assert.equal(postListCache.has('admin:posts'), true);
  postListCache.clear();
});
