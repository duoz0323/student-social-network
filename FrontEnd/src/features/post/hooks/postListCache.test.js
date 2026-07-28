import assert from 'node:assert/strict';
import test from 'node:test';
import { invalidatePostListCaches, postListCache } from './postListCache.js';

test('chỉ vô hiệu hóa cache có prefix được yêu cầu', () => {
  postListCache.clear();
  postListCache.set('feed:for-you', { posts: [1] });
  postListCache.set('profile-posts:2', { posts: [2] });
  postListCache.set('admin:posts', { posts: [3] });

  invalidatePostListCaches(['feed:', 'profile-posts:']);

  assert.equal(postListCache.has('feed:for-you'), false);
  assert.equal(postListCache.has('profile-posts:2'), false);
  assert.equal(postListCache.has('admin:posts'), true);
  postListCache.clear();
});
