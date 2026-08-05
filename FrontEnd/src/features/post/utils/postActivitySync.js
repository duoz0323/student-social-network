const LOCAL_EVENT_NAME = 'unishare:post-activity';
const CHANNEL_NAME = 'unishare.post-activity';
const STORAGE_KEY = 'unishare.post-activity.event';

let channel = null;
let nextEventSequence = 1;

function createEventId() {
  const randomId = globalThis.crypto?.randomUUID?.();
  if (randomId) return randomId;
  const eventId = `${Date.now()}-${nextEventSequence}`;
  nextEventSequence += 1;
  return eventId;
}

function getChannel() {
  if (channel || typeof BroadcastChannel !== 'function') return channel;
  channel = new BroadcastChannel(CHANNEL_NAME);
  return channel;
}

function normalizeActivity(activity) {
  if (!activity?.postId) return null;
  return {
    ...activity,
    postId: String(activity.postId),
    eventId: activity.eventId || createEventId(),
    occurredAt: activity.occurredAt || new Date().toISOString(),
  };
}

/** Phát thay đổi trong tab hiện tại và sang các tab UniShare khác của cùng trình duyệt. */
export function publishPostActivity(activity) {
  const event = normalizeActivity(activity);
  if (!event || typeof window === 'undefined') return null;

  window.dispatchEvent(new CustomEvent(LOCAL_EVENT_NAME, { detail: event }));
  const activeChannel = getChannel();
  if (activeChannel) {
    activeChannel.postMessage(event);
  } else {
    try {
      localStorage.setItem(STORAGE_KEY, JSON.stringify(event));
      localStorage.removeItem(STORAGE_KEY);
    } catch {
      // Storage có thể bị trình duyệt chặn; đồng bộ trong tab hiện tại vẫn hoạt động.
    }
  }
  return event;
}

/** Đăng ký một listener duy nhất cho cả sự kiện cùng tab và sự kiện từ tab khác. */
export function subscribePostActivity(listener) {
  if (typeof window === 'undefined' || typeof listener !== 'function') return () => {};

  const handleLocal = (event) => listener(event.detail);
  const handleStorage = (event) => {
    if (event.key !== STORAGE_KEY || !event.newValue) return;
    try {
      listener(JSON.parse(event.newValue));
    } catch {
      // Bỏ qua payload không đúng contract và chờ lần reconciliation REST tiếp theo.
    }
  };
  const activeChannel = getChannel();
  const handleChannel = (event) => listener(event.data);

  window.addEventListener(LOCAL_EVENT_NAME, handleLocal);
  if (activeChannel) activeChannel.addEventListener('message', handleChannel);
  else window.addEventListener('storage', handleStorage);

  return () => {
    window.removeEventListener(LOCAL_EVENT_NAME, handleLocal);
    if (activeChannel) activeChannel.removeEventListener('message', handleChannel);
    else window.removeEventListener('storage', handleStorage);
  };
}

export function isSamePost(post, postId) {
  return String(post?.id ?? post?.postId ?? '') === String(postId ?? '');
}

/** Áp dụng counter và trạng thái viewer mới nhất lên mọi snapshot của cùng một Post. */
export function applyPostActivity(post, activity, currentUserId) {
  if (!isSamePost(post, activity?.postId)) return post;

  const next = { ...post };
  if (Number.isFinite(activity.likeCount)) next.likeCount = Math.max(0, activity.likeCount);
  if (Number.isFinite(activity.commentCount)) next.commentCount = Math.max(0, activity.commentCount);
  if (Number.isFinite(activity.repostCount)) next.repostCount = Math.max(0, activity.repostCount);

  const affectsCurrentViewer = activity.viewerUserId != null
    && String(activity.viewerUserId) === String(currentUserId);
  if (affectsCurrentViewer && typeof activity.likedByCurrentUser === 'boolean') {
    next.likedByCurrentUser = activity.likedByCurrentUser;
  }
  if (affectsCurrentViewer && typeof activity.savedByCurrentUser === 'boolean') {
    next.savedByCurrentUser = activity.savedByCurrentUser;
  }
  if (affectsCurrentViewer && typeof activity.repostedByCurrentUser === 'boolean') {
    next.repostedByCurrentUser = activity.repostedByCurrentUser;
  }
  return next;
}

export function getListMembership(activity, cacheKey) {
  return activity?.memberships?.find((item) => item.cacheKey === cacheKey) ?? null;
}

