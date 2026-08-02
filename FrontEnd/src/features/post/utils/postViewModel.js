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
    repostedByCurrentUser: post.repostedByCurrentUser ?? false,
    repostCount: Number(post.repostCount) || 0,
  };
}

/** Chuẩn hóa activity Following/Profile Repost nhưng không làm mất metadata người đăng lại. */
export function toFeedItemView(item = {}) {
  const post = toPostView(item.post ?? item);
  const itemType = item.itemType ?? 'ORIGINAL';
  return {
    ...post,
    itemType,
    activityAt: item.activityAt ?? post.publishedAt,
    repostedAt: item.repostedAt ?? null,
    repostedBy: item.repostedBy ?? null,
    feedItemKey: `${itemType}:${item.repostedBy?.id ?? post.authorId}:${post.id}:${item.activityAt ?? post.publishedAt ?? ''}`,
  };
}

/**
 * Tạo snapshot chỉnh sửa từ Post Detail để form không dùng dữ liệu rút gọn hoặc đã cũ của danh sách.
 * Danh sách ID media là trạng thái ban đầu để form có thể giữ hoặc gỡ từng media khi cập nhật.
 */
export function toPostEditDraft(postDetail = {}) {
  const post = toPostView(postDetail);
  return {
    post,
    content: post.content ?? '',
    hashtag: post.hashtag ?? post.hashtags?.[0] ?? '',
    location: post.location ?? null,
    keepMediaIds: (post.media ?? []).map((item) => item.id),
  };
}

/** Tạo và sao chép permalink của bài viết tại một điểm duy nhất. */
export function copyPostLink(postId) {
  const url = new URL(`/posts/${encodeURIComponent(postId)}`, window.location.origin).toString();
  return navigator.clipboard?.writeText(url);
}
