import {
  applyPostActivity,
  getListMembership,
  isSamePost,
  subscribePostActivity,
} from '../utils/postActivitySync.js';
import { tokenManager } from '../../../api/tokenManager.js';

/** Cache dùng chung cho các danh sách bài viết cursor; tách riêng để Block có thể vô hiệu hóa tập trung. */
export const postListCache = new Map();

export function invalidatePostListCaches(prefixes) {
  for (const key of postListCache.keys()) {
    if (prefixes.some((prefix) => key.startsWith(prefix))) {
      postListCache.delete(key);
    }
  }
}

// Listener cấp module giữ cache của cả những màn hình đang unmount không bị phục hồi dữ liệu cũ.
subscribePostActivity((activity) => {
  const viewerUserId = tokenManager.getSessionSnapshot()?.user?.id ?? null;
  for (const [cacheKey, cachedValue] of postListCache.entries()) {
    const membership = getListMembership(activity, cacheKey);
    const explicitlyInvalidated = activity?.invalidateCacheKeys?.includes(cacheKey);
    const containsPost = cachedValue.posts.some((post) => isSamePost(post, activity?.postId));

    if (membership || explicitlyInvalidated || (activity?.requiresReconcile && containsPost)) {
      postListCache.delete(cacheKey);
      continue;
    }

    if (containsPost) {
      postListCache.set(cacheKey, {
        ...cachedValue,
        posts: cachedValue.posts.map((post) => applyPostActivity(post, activity, viewerUserId)),
      });
    }
  }
});
