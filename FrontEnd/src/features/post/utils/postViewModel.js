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

export const FACEBOOK_SHARE_STATUS = Object.freeze({
  OPENED: 'OPENED',
  PUBLIC_URL_REQUIRED: 'PUBLIC_URL_REQUIRED',
  POPUP_BLOCKED: 'POPUP_BLOCKED',
});

function configuredPublicOrigin() {
  return import.meta.env?.VITE_PUBLIC_APP_URL?.trim()
    || globalThis.window?.location?.origin
    || 'http://localhost';
}

/** Tạo permalink tuyệt đối từ route Post Detail, ưu tiên domain public cấu hình cho external share. */
export function canonicalPostUrl(postId, origin = configuredPublicOrigin()) {
  return new URL(`/posts/${encodeURIComponent(postId)}`, origin).toString();
}

function isPublicShareUrl(value) {
  try {
    const url = new URL(value);
    const host = url.hostname.toLowerCase();
    const privateIpv4 = /^10\.|^192\.168\.|^127\.|^172\.(1[6-9]|2\d|3[01])\./.test(host);
    return ['http:', 'https:'].includes(url.protocol)
      && host !== 'localhost'
      && host !== '::1'
      && !host.endsWith('.localhost')
      && !host.endsWith('.local')
      && !privateIpv4;
  } catch {
    return false;
  }
}

/** Sao chép permalink và dùng fallback DOM cho browser chưa có Clipboard API. */
export async function copyPostLink(postId) {
  const url = canonicalPostUrl(postId);
  if (navigator.clipboard?.writeText) {
    await navigator.clipboard.writeText(url);
    return url;
  }
  if (!globalThis.document?.createElement) {
    throw new Error('Trình duyệt không hỗ trợ sao chép tự động.');
  }
  const input = globalThis.document.createElement('textarea');
  input.value = url;
  input.setAttribute('readonly', '');
  input.style.position = 'fixed';
  input.style.opacity = '0';
  globalThis.document.body.appendChild(input);
  input.select();
  const copied = globalThis.document.execCommand?.('copy');
  input.remove();
  if (!copied) throw new Error('Trình duyệt không hỗ trợ sao chép tự động.');
  return url;
}

/** Mở Facebook Share Dialog/Web Sharer bằng URL public, không dùng provider token. */
export function openFacebookPostShare(
  postId,
  openWindow = globalThis.window?.open,
  facebookSdk = globalThis.window?.FB,
  origin,
) {
  const postUrl = canonicalPostUrl(postId, origin ?? configuredPublicOrigin());
  if (!isPublicShareUrl(postUrl)) return FACEBOOK_SHARE_STATUS.PUBLIC_URL_REQUIRED;

  if (typeof facebookSdk?.ui === 'function') {
    facebookSdk.ui({ method: 'share', href: postUrl });
    return FACEBOOK_SHARE_STATUS.OPENED;
  }
  const shareUrl = new URL('https://www.facebook.com/sharer/sharer.php');
  shareUrl.searchParams.set('u', postUrl);
  const popup = openWindow?.(shareUrl.toString(), 'facebook-share', 'popup,width=640,height=720');
  if (!popup) return FACEBOOK_SHARE_STATUS.POPUP_BLOCKED;
  // Cắt liên kết về cửa sổ gốc ngay sau khi đã lấy handle để vừa chống tabnabbing vừa phát hiện popup block.
  try { popup.opener = null; } catch { /* Browser có thể không cho ghi thuộc tính cross-origin. */ }
  return FACEBOOK_SHARE_STATUS.OPENED;
}

/**
 * Ưu tiên tác giả mới nhất do API Post/Feed trả về; cache chỉ dùng để bổ sung field còn thiếu.
 * Điều này tránh avatar vừa upload bị dữ liệu user cũ trong AppContext ghi đè thành rỗng.
 */
export function resolvePostAuthor(post = {}, cachedAuthor = null) {
  const responseAuthor = post.author ?? {};

  return {
    ...(cachedAuthor ?? {}),
    ...responseAuthor,
    id: responseAuthor.id ?? cachedAuthor?.id ?? post.authorId ?? null,
    displayName: responseAuthor.displayName || cachedAuthor?.displayName || 'Người dùng UniShare',
    avatarUrl: responseAuthor.avatarUrl || cachedAuthor?.avatarUrl || '',
  };
}
