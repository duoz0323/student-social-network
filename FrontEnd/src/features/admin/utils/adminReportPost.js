function normalizeMedia(media, postId) {
  if (!Array.isArray(media)) return [];

  return media.map((item, index) => ({
    id: item.mediaId ?? item.id ?? `${postId}-media-${index}`,
    url: item.mediaUrl ?? item.url,
    mediaType: item.mediaType ?? 'IMAGE',
    mimeType: item.mimeType,
    thumbnailUrl: item.thumbnailUrl,
    displayOrder: item.sortOrder ?? item.displayOrder ?? index,
    width: item.widthPx ?? item.width,
    height: item.heightPx ?? item.height,
  })).filter((item) => Boolean(item.url));
}

/** Chuẩn hóa Admin Post Detail về cấu trúc hiển thị dùng chung của thẻ bài viết. */
export function toAdminReportPostView(postDetail) {
  if (!postDetail) return null;

  const postId = postDetail.postId ?? postDetail.id;
  const authorId = postDetail.author?.userId ?? postDetail.author?.id;

  return {
    id: postId,
    content: postDetail.content ?? '',
    status: postDetail.status,
    createdAt: postDetail.createdAt,
    publishedAt: postDetail.publishedAt ?? postDetail.createdAt,
    authorId,
    author: postDetail.author
      ? {
          id: authorId,
          displayName: postDetail.author.displayName,
          avatarUrl: postDetail.author.avatarUrl,
        }
      : null,
    hashtags: postDetail.hashtag ? [postDetail.hashtag] : [],
    media: normalizeMedia(postDetail.media, postId),
    location: postDetail.location ?? null,
  };
}

/** Giữ nội dung bằng chứng khả dụng nếu API chi tiết bài viết tạm thời không tải được. */
export function toReportPostFallback(report) {
  const reportedPost = report?.reportedPost;
  if (!reportedPost) return null;

  return {
    id: reportedPost.postId,
    content: reportedPost.currentContent ?? report.evidence?.contentSnapshot ?? '',
    status: reportedPost.currentStatus,
    createdAt: report.createdAt,
    publishedAt: report.createdAt,
    authorId: reportedPost.author?.userId,
    author: reportedPost.author
      ? {
          id: reportedPost.author.userId,
          displayName: reportedPost.author.displayName,
          avatarUrl: reportedPost.author.avatarUrl,
        }
      : null,
    hashtags: [],
    media: normalizeMedia(
      report.evidence?.mediaSnapshot?.map((mediaUrl, index) => ({ mediaUrl, sortOrder: index })),
      reportedPost.postId,
    ),
    location: null,
  };
}
