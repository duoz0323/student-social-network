/**
 * Chuẩn hóa response Post từ Feed, Search và Post Detail về một shape duy nhất cho PostCard.
 * Hàm luôn giữ lại field gốc để các màn hình không mất dữ liệu riêng của endpoint.
 */
export function toPostView(post = {}) {
  return {
    ...post,
    id: post.postId ?? post.id,
    authorId: post.author?.id ?? post.authorId,
    imageUrls: (post.media ?? []).map((item) => item.url),
    hashtags: post.hashtag ? [post.hashtag] : (post.hashtags ?? []),
    edited: post.isEdited ?? post.edited ?? false,
    createdAt: post.createdAt ?? post.publishedAt,
    likedByCurrentUser: post.likedByCurrentUser ?? post.viewer?.likedByCurrentUser ?? false,
    savedByCurrentUser: post.savedByCurrentUser ?? post.viewer?.savedByCurrentUser ?? false,
  };
}

/** Tạo và sao chép permalink của bài viết tại một điểm duy nhất. */
export function copyPostLink(postId) {
  const url = new URL(`/posts/${encodeURIComponent(postId)}`, window.location.origin).toString();
  return navigator.clipboard?.writeText(url);
}
