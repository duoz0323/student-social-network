/** Cache dùng chung cho các danh sách bài viết cursor; tách riêng để Block có thể vô hiệu hóa tập trung. */
export const postListCache = new Map();

export function invalidatePostListCaches(prefixes) {
  for (const key of postListCache.keys()) {
    if (prefixes.some((prefix) => key.startsWith(prefix))) {
      postListCache.delete(key);
    }
  }
}
