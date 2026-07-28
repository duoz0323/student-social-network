import { invalidatePostListCaches } from '../../post/hooks/postListCache.js';

const BLOCK_RELATED_POST_PREFIXES = [
  'feed:',
  'profile-posts:',
  'liked-posts',
  'saved-posts',
  'search-posts:',
];

/** Xóa đúng các cache bài viết có thể chứa dữ liệu của tài khoản vừa bị chặn. */
export function invalidateUserBlockCaches() {
  invalidatePostListCaches(BLOCK_RELATED_POST_PREFIXES);
}

/** Đồng bộ snapshot Context để UI không giữ quan hệ hoặc nội dung cũ sau khi Block. */
export function removeBlockedUserFromState(data, currentUserId, targetUserId) {
  const current = String(currentUserId);
  const target = String(targetUserId);
  const removedPostIds = new Set(
    data.posts
      .filter((post) => String(post.authorId ?? post.author?.id) === target)
      .map((post) => String(post.id ?? post.postId)),
  );
  const hiddenCommentIds = new Set(
    data.comments
      .filter((comment) => {
        const authorId = String(comment.authorId ?? comment.userId);
        return authorId === target || removedPostIds.has(String(comment.postId));
      })
      .map((comment) => String(comment.id ?? comment.commentId)),
  );

  // Khi comment cha bị ẩn do Block, loại luôn reply của nhánh đó để không tạo nội dung mồ côi.
  for (const comment of data.comments) {
    if (comment.parentCommentId != null
        && hiddenCommentIds.has(String(comment.parentCommentId))) {
      hiddenCommentIds.add(String(comment.id ?? comment.commentId));
    }
  }

  return {
    ...data,
    users: data.users.filter((user) => String(user.id) !== target),
    posts: data.posts.filter((post) => !removedPostIds.has(String(post.id ?? post.postId))),
    follows: data.follows.filter((follow) => {
      const follower = String(follow.followerId);
      const following = String(follow.followingId);
      return !((follower === current && following === target)
        || (follower === target && following === current));
    }),
    likes: data.likes.filter((like) => !removedPostIds.has(String(like.postId))),
    savedPosts: data.savedPosts.filter((saved) => !removedPostIds.has(String(saved.postId))),
    comments: data.comments.filter(
      (comment) => !hiddenCommentIds.has(String(comment.id ?? comment.commentId)),
    ),
  };
}
