import { useState, useRef, useEffect } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { Shield } from 'lucide-react';
import Avatar from '../../../components/common/Avatar.jsx';
import Button from '../../../components/common/Button.jsx';
import Modal from '../../../components/common/Modal.jsx';
import { LoadingState } from '../../../components/common/StateBlock.jsx';
import { useApp } from '../../../contexts/AppContext.jsx';
import { shortTime, formatNumber } from '../../../utils/formatters.js';
import PostMediaGrid from './PostMediaGrid.jsx';
import ReportPostFlow from './ReportPostFlow.jsx';
import UserRestrictionAction from '../../profile/components/UserRestrictionAction.jsx';
import EditPostMedia from './EditPostMedia.jsx';
import PostHashtagPicker from './PostHashtagPicker.jsx';
import { copyPostLink, resolvePostAuthor, toPostEditDraft, toPostView } from '../utils/postViewModel.js';
import { formatPostEditCountdown, postEditRemainingSeconds } from '../utils/postEditWindow.js';
import LocationPicker from '../locations/LocationPicker.jsx';
import SelectedLocation from '../locations/SelectedLocation.jsx';
import { googleMapsLocationUrl } from '../locations/locationUtils.js';
import { resolveLocationUpdate } from '../locations/locationMultipart.js';
import { postApi } from '../../../api/index.js';
import { publishPostActivity, subscribePostActivity } from '../utils/postActivitySync.js';

function SuccessIcon() {
  return (
    <div className="mx-auto mb-3 flex h-12 w-12 items-center justify-center rounded-full bg-[var(--app-surface-soft)] text-[var(--app-text)]">
      <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round">
        <polyline points="20 6 9 17 4 12" />
      </svg>
    </div>
  );
}

// === SVG Icon Components — thay thế Unicode ký tự cho chuyên nghiệp hơn ===

function HeartIcon({ filled }) {
  return filled ? (
    <svg width="18" height="18" viewBox="0 0 24 24" fill="#ef4444" stroke="#ef4444" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
      <path d="M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z" />
    </svg>
  ) : (
    <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
      <path d="M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z" />
    </svg>
  );
}

function CommentIcon() {
  return (
    <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
      <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z" />
    </svg>
  );
}

// Icon Repost dùng cho thao tác idempotent với bài gốc.
function RepostIcon() {
  return (
    <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
      <path d="M17 1l4 4-4 4" />
      <path d="M3 11V9a4 4 0 0 1 4-4h14" />
      <path d="M7 23l-4-4 4-4" />
      <path d="M21 13v2a4 4 0 0 1-4 4H3" />
    </svg>
  );
}

function ShareIcon() {
  return (
    <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
      <path d="M4 12v8a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2v-8" />
      <polyline points="16 6 12 2 8 6" />
      <line x1="12" x2="12" y1="2" y2="15" />
    </svg>
  );
}

// === Icon nhỏ cho dropdown menu ===

function BookmarkIcon() {
  return (
    <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
      <path d="M19 21l-7-5-7 5V5a2 2 0 0 1 2-2h10a2 2 0 0 1 2 2z" />
    </svg>
  );
}

function EditIcon() {
  return (
    <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
      <path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7" />
      <path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z" />
    </svg>
  );
}

function TrashIcon() {
  return (
    <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
      <polyline points="3 6 5 6 21 6" />
      <path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2" />
    </svg>
  );
}

function FlagIcon() {
  return (
    <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
      <path d="M4 15s1-1 4-1 5 2 8 2 4-1 4-1V3s-1 1-4 1-5-2-8-2-4 1-4 1z" />
      <line x1="4" x2="4" y1="22" y2="15" />
    </svg>
  );
}

function LinkIcon() {
  return (
    <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
      <path d="M10 13a5 5 0 0 0 7.54.54l3-3a5 5 0 0 0-7.07-7.07l-1.72 1.71" />
      <path d="M14 11a5 5 0 0 0-7.54-.54l-3 3a5 5 0 0 0 7.07 7.07l1.71-1.71" />
    </svg>
  );
}

export default function PostCard({
  post: initialPost,
  detail = false,
  locationMeta = '',
  onSaveChange,
  onLikeChange,
  onRepostChange,
  showRepostAttribution = true,
}) {
  const navigate = useNavigate();
  const {
    currentUserId, getUserById, data, toggleLike, toggleSave,
    getPostDetail, updatePost, deletePost, showToast,
  } = useApp();
  const [updatedPost, setUpdatedPost] = useState(null);
  const post = updatedPost ?? initialPost;
  const content = post.content ?? '';
  const hashtags = Array.isArray(post.hashtags) ? post.hashtags : [];
  const [menuOpen, setMenuOpen] = useState(false);
  const [editing, setEditing] = useState(false);
  const [deleteStep, setDeleteStep] = useState(null); // null | 'confirm' | 'success'
  const [reporting, setReporting] = useState(false);
  const [restrictionConfirmOpen, setRestrictionConfirmOpen] = useState(false);
  const [restrictedByMe, setRestrictedByMe] = useState(Boolean(initialPost.author?.restrictedByMe));
  const [draft, setDraft] = useState(content);
  const [tags, setTags] = useState(hashtags.join(', '));
  const [editLocation, setEditLocation] = useState(post.location ?? null);
  const [editSource, setEditSource] = useState(null);
  const [editLoading, setEditLoading] = useState(false);
  const [editSubmitting, setEditSubmitting] = useState(false);
  const [editMediaBusy, setEditMediaBusy] = useState(false);
  const [editMediaSelection, setEditMediaSelection] = useState({ keepMediaIds: [], newMediaFiles: [], totalCount: 0 });
  const [editError, setEditError] = useState('');
  const [locationPickerOpen, setLocationPickerOpen] = useState(false);
  const menuRef = useRef(null);
  const editRequestRef = useRef(null);
  const syncRequestRef = useRef(null);
  const author = resolvePostAuthor(post, getUserById(post.authorId));
  const isOwner = post.authorId === currentUserId;
  const initialLiked = post.likedByCurrentUser
    ?? data.likes.some((like) => String(like.postId) === String(post.id) && String(like.userId) === String(currentUserId));
  const initialSaved = post.savedByCurrentUser
    ?? data.savedPosts.some((item) => String(item.postId) === String(post.id) && String(item.userId) === String(currentUserId));
  const [liked, setLiked] = useState(initialLiked);
  const [saved, setSaved] = useState(initialSaved);
  const [reposted, setReposted] = useState(Boolean(post.repostedByCurrentUser));
  const [repostCount, setRepostCount] = useState(Number(post.repostCount) || 0);
  const [repostSubmitting, setRepostSubmitting] = useState(false);
  const [likeSubmitting, setLikeSubmitting] = useState(false);
  const [likeCountFromInteraction, setLikeCountFromInteraction] = useState(null);
  const [commentCountFromInteraction, setCommentCountFromInteraction] = useState(null);
  const [syncedInitialPost, setSyncedInitialPost] = useState(initialPost);
  const [editClockMs, setEditClockMs] = useState(() => Date.now());
  // Ưu tiên số mới nhất do API Like/Unlike trả về; trước tương tác dùng dữ liệu Feed/Post Detail.
  const likeCount = likeCountFromInteraction ?? (Number(post.likeCount) || 0);
  const commentCount = commentCountFromInteraction ?? (Number(post.commentCount) || 0);
  const editRemainingSeconds = postEditRemainingSeconds(post.publishedAt ?? post.createdAt, editClockMs);
  const canShowEdit = editRemainingSeconds === null || editRemainingSeconds > 0;
  const shouldRunEditClock = isOwner && menuOpen && editRemainingSeconds !== null && editRemainingSeconds > 0;

  useEffect(() => () => {
    editRequestRef.current?.abort();
    syncRequestRef.current?.abort();
  }, []);

  if (syncedInitialPost !== initialPost) {
    setSyncedInitialPost(initialPost);
    setUpdatedPost(null);
    // PostCard đổi dữ liệu đầu vào thì lấy lại trạng thái quan hệ từ snapshot mới.
    setRestrictedByMe(Boolean(initialPost.author?.restrictedByMe));
    setLiked(Boolean(initialPost.likedByCurrentUser));
    setSaved(Boolean(initialPost.savedByCurrentUser));
    setReposted(Boolean(initialPost.repostedByCurrentUser));
    setLikeCountFromInteraction(Number(initialPost.likeCount) || 0);
    setCommentCountFromInteraction(Number(initialPost.commentCount) || 0);
    setRepostCount(Number(initialPost.repostCount) || 0);
  }

  useEffect(() => subscribePostActivity((activity) => {
    if (String(activity?.postId) !== String(post.id)) return;

    if (Number.isFinite(activity.likeCount)) setLikeCountFromInteraction(Math.max(0, activity.likeCount));
    if (Number.isFinite(activity.commentCount)) setCommentCountFromInteraction(Math.max(0, activity.commentCount));
    if (Number.isFinite(activity.repostCount)) setRepostCount(Math.max(0, activity.repostCount));

    const affectsCurrentViewer = activity.viewerUserId != null
      && String(activity.viewerUserId) === String(currentUserId);
    if (affectsCurrentViewer && typeof activity.likedByCurrentUser === 'boolean') setLiked(activity.likedByCurrentUser);
    if (affectsCurrentViewer && typeof activity.savedByCurrentUser === 'boolean') setSaved(activity.savedByCurrentUser);
    if (affectsCurrentViewer && typeof activity.repostedByCurrentUser === 'boolean') setReposted(activity.repostedByCurrentUser);

    if (activity.requiresReconcile !== true) return;
    syncRequestRef.current?.abort();
    const controller = new AbortController();
    syncRequestRef.current = controller;
    postApi.getDetail(post.id, controller.signal)
      .then((response) => {
        if (controller.signal.aborted) return;
        const snapshot = toPostView(response);
        setUpdatedPost(snapshot);
        setLiked(Boolean(snapshot.likedByCurrentUser));
        setSaved(Boolean(snapshot.savedByCurrentUser));
        setReposted(Boolean(snapshot.repostedByCurrentUser));
        setLikeCountFromInteraction(Number(snapshot.likeCount) || 0);
        setCommentCountFromInteraction(Number(snapshot.commentCount) || 0);
        setRepostCount(Number(snapshot.repostCount) || 0);
      })
      .catch(() => {
        // REST reconciliation ở list/foreground sẽ thử lại nếu request realtime tạm thời thất bại.
      });
  }), [currentUserId, post.id]);

  useEffect(() => {
    if (!shouldRunEditClock) return undefined;
    const timer = window.setInterval(() => setEditClockMs(Date.now()), 1000);
    return () => window.clearInterval(timer);
  }, [shouldRunEditClock]);

  // Đóng menu khi click ra ngoài
  useEffect(() => {
    if (!menuOpen) return;
    function handleClickOutside(event) {
      if (menuRef.current && !menuRef.current.contains(event.target)) {
        setMenuOpen(false);
      }
    }
    document.addEventListener('mousedown', handleClickOutside);
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, [menuOpen]);

  if (!author) return null;

  async function loadEditDetail() {
    editRequestRef.current?.abort();
    const controller = new AbortController();
    editRequestRef.current = controller;
    setEditLoading(true);
    setEditError('');

    try {
      const postDetail = await getPostDetail(post.id, controller.signal);
      if (controller.signal.aborted) return;
      const editDraft = toPostEditDraft(postDetail);
      setEditSource(editDraft);
      setDraft(editDraft.content);
      setTags(editDraft.hashtag);
      setEditLocation(editDraft.location);
      setEditMediaSelection({
        keepMediaIds: editDraft.keepMediaIds,
        newMediaFiles: [],
        totalCount: editDraft.keepMediaIds.length,
      });
    } catch (requestError) {
      if (requestError.code !== 'ERR_CANCELED') {
        setEditError(requestError.message || 'Không thể tải chi tiết bài viết.');
      }
    } finally {
      if (editRequestRef.current === controller) {
        editRequestRef.current = null;
        setEditLoading(false);
      }
    }
  }

  function openEdit() {
    setEditSource(null);
    setEditError('');
    setLocationPickerOpen(false);
    setEditing(true);
    loadEditDetail();
  }

  function closeEdit() {
    if (editSubmitting) return;
    editRequestRef.current?.abort();
    editRequestRef.current = null;
    setEditing(false);
    setLocationPickerOpen(false);
  }

  async function saveEdit() {
    if (!editSource || editLoading || editSubmitting || editMediaBusy) return;
    const locationUpdate = resolveLocationUpdate(editSource.location, editLocation);
    setEditSubmitting(true);
    setEditError('');

    try {
      const response = await updatePost(post.id, {
        content: draft,
        hashtag: tags,
        keepMediaIds: editMediaSelection.keepMediaIds,
        newMediaFiles: editMediaSelection.newMediaFiles,
        ...locationUpdate,
      });
      // Danh sách Feed/Profile/Search có cache riêng, nên PostCard cập nhật ngay từ response PUT.
      setUpdatedPost(toPostView(response));
      setEditing(false);
      setLocationPickerOpen(false);
    } catch (requestError) {
      setEditError(requestError.message || 'Không thể cập nhật bài viết.');
    } finally {
      setEditSubmitting(false);
    }
  }

  function confirmDelete() {
    setDeleteStep('success');
  }

  function executeDelete() {
    deletePost(post.id);
    setDeleteStep(null);
    if (detail) navigate('/feed/for-you');
  }

  async function handleLike() {
    if (likeSubmitting) return;
    const previousLiked = liked;
    const previousLikeCount = likeCount;
    const optimisticLiked = !previousLiked;

    // Phản hồi ngay trên UI; response Backend sau đó chốt lại counter authoritative.
    setLikeSubmitting(true);
    setLiked(optimisticLiked);
    setLikeCountFromInteraction(Math.max(0, previousLikeCount + (optimisticLiked ? 1 : -1)));
    try {
      const response = await toggleLike(post.id, previousLiked);
      setLiked(response.likedByCurrentUser);
      setLikeCountFromInteraction(Number(response.likeCount) || 0);
      publishPostActivity({
        postId: post.id,
        viewerUserId: currentUserId,
        likedByCurrentUser: Boolean(response.likedByCurrentUser),
        likeCount: Number(response.likeCount) || 0,
        memberships: [{ cacheKey: 'posts:liked', included: Boolean(response.likedByCurrentUser) }],
      });
      // Cho màn hình Đã thích loại bài khỏi danh sách ngay khi người dùng Unlike.
      onLikeChange?.(post.id, response.likedByCurrentUser);
    } catch (error) {
      setLiked(previousLiked);
      setLikeCountFromInteraction(previousLikeCount);
      showToast(error.message || 'Không thể cập nhật lượt thích. Vui lòng thử lại.', 'error');
    } finally {
      setLikeSubmitting(false);
    }
  }

  async function handleSave() {
    try {
      const response = await toggleSave(post.id, saved);
      setSaved(response.saved);
      publishPostActivity({
        postId: post.id,
        viewerUserId: currentUserId,
        savedByCurrentUser: Boolean(response.saved),
        memberships: [{ cacheKey: 'posts:saved', included: Boolean(response.saved) }],
      });
      // Cho màn hình Saved loại bài khỏi danh sách ngay khi người dùng bỏ lưu.
      onSaveChange?.(post.id, response.saved);
      showToast(response.saved ? 'Đã lưu bài viết.' : 'Đã bỏ lưu bài viết.');
    } catch (error) {
      showToast(error.message || 'Không thể cập nhật bài viết đã lưu. Vui lòng thử lại.', 'error');
    }
  }

  async function handleRepost() {
    if (isOwner || repostSubmitting) return;
    setRepostSubmitting(true);
    try {
      // PUT/DELETE đều idempotent; UI luôn lấy lại trạng thái và counter do Backend trả về.
      const response = reposted ? await postApi.unrepost(post.id) : await postApi.repost(post.id);
      setReposted(Boolean(response.repostedByCurrentUser));
      setRepostCount(Number(response.repostCount) || 0);
      publishPostActivity({
        postId: post.id,
        viewerUserId: currentUserId,
        repostedByCurrentUser: Boolean(response.repostedByCurrentUser),
        repostCount: Number(response.repostCount) || 0,
        memberships: [{
          cacheKey: `profile-reposts:${currentUserId}`,
          included: Boolean(response.repostedByCurrentUser),
        }],
        invalidateCacheKeys: ['feed:following'],
      });
      onRepostChange?.(post.id, Boolean(response.repostedByCurrentUser));
    } finally {
      setRepostSubmitting(false);
    }
  }

  async function handleCopyPostLink() {
    try {
      await copyPostLink(post.id);
      showToast('Đã sao chép liên kết.');
    } catch {
      showToast('Không thể sao chép liên kết. Vui lòng thử lại.', 'error');
    }
  }

  // Đóng menu sau khi chọn hành động
  function menuAction(action) {
    setMenuOpen(false);
    action();
  }

  return (
    <article className="post-card grid grid-cols-[42px_minmax(0,1fr)] gap-3 border-b border-[var(--app-border-strong)] bg-[var(--app-surface)] px-5 py-4">
      {/* Avatar tác giả — bấm vào điều hướng đến trang cá nhân */}
      <Link to={author.id === currentUserId ? '/profile/me' : `/profile/${author.id}`} className="pt-0.5">
        <Avatar src={author.avatarUrl} name={author.displayName} />
      </Link>

      <div className="min-w-0">
        {showRepostAttribution && post.itemType === 'REPOST' && post.repostedBy && (
          <Link
            to={String(post.repostedBy.id) === String(currentUserId) ? '/profile/me' : `/profile/${post.repostedBy.id}`}
            className="mb-2 block text-[13px] font-semibold text-[var(--app-muted)] hover:text-[var(--app-text)]"
          >
            {post.repostedBy.displayName} đã đăng lại
          </Link>
        )}
        {/* Header: tên tác giả + thời gian + nút menu */}
        <header className="flex items-start justify-between gap-3">
          <Link to={author.id === currentUserId ? '/profile/me' : `/profile/${author.id}`} className="min-w-0">
            <p className="truncate text-[15px] font-bold text-[var(--app-text)]">
              {author.displayName}
              <span className="ml-1.5 font-normal text-[var(--app-muted)]">
                · {shortTime(post.createdAt)}
                {post.edited ? ' · đã sửa' : ''}
              </span>
            </p>
          </Link>

          {/* Nút menu "..." — dropdown overlay thay vì inline panel */}
          <div className="relative" ref={menuRef}>
            <button
              className="flex h-8 w-8 items-center justify-center rounded-full text-[var(--app-muted)] transition hover:bg-[var(--app-surface-soft)]"
              onClick={() => {
                // Đồng bộ thời gian ngay khi mở menu; interval chỉ chạy khi countdown đang hiển thị.
                setEditClockMs(Date.now());
                setMenuOpen((open) => !open);
              }}
              aria-label="Thao tác bài viết"
            >
              <svg width="18" height="18" viewBox="0 0 24 24" fill="currentColor">
                <circle cx="12" cy="5" r="1.5" />
                <circle cx="12" cy="12" r="1.5" />
                <circle cx="12" cy="19" r="1.5" />
              </svg>
            </button>

            {menuOpen && (
              <div className="post-menu-dropdown">
                {isOwner ? (
                  <>
                    <button onClick={() => menuAction(handleSave)}>
                      <span>{saved ? 'Bỏ lưu' : 'Lưu'}</span>
                      <BookmarkIcon />
                    </button>
                    {canShowEdit && (
                      <button onClick={() => menuAction(openEdit)}>
                        <span className="flex min-w-0 items-center gap-2">
                          <span>Chỉnh sửa</span>
                          {editRemainingSeconds !== null && (
                            <span className="font-mono text-xs text-[var(--app-muted)]">
                              {formatPostEditCountdown(editRemainingSeconds)}
                            </span>
                          )}
                        </span>
                        <EditIcon />
                      </button>
                    )}
                    <button onClick={() => menuAction(() => setDeleteStep('confirm'))}>
                      <span>Xóa</span>
                      <TrashIcon />
                    </button>
                  </>
                ) : (
                  <>
                    <button onClick={() => menuAction(handleSave)}>
                      <span>{saved ? 'Bỏ lưu' : 'Lưu'}</span>
                      <BookmarkIcon />
                    </button>
                    <button onClick={() => menuAction(() => setReporting(true))}>
                      <span>Báo cáo</span>
                      <FlagIcon />
                    </button>
                    {!author.blockedByMe ? (
                      <button onClick={() => menuAction(() => setRestrictionConfirmOpen(true))}>
                        <span>{restrictedByMe ? 'Bỏ hạn chế' : 'Hạn chế'}</span>
                        <Shield size={16} strokeWidth={2} aria-hidden="true" />
                      </button>
                    ) : null}
                  </>
                )}
                <button onClick={() => menuAction(handleCopyPostLink)}>
                  <span>Sao chép liên kết</span>
                  <LinkIcon />
                </button>
              </div>
            )}
          </div>
        </header>

        {/* Nội dung bài viết — bấm vào mở chi tiết nếu không phải trang detail */}
        <button className="mt-1 block w-full text-left" onClick={() => !detail && navigate(`/posts/${post.id}`)}>
          {content && <p className="whitespace-pre-line text-[15px] leading-6 text-[var(--app-text)]">{content}</p>}
          {hashtags.length ? (
            <div className="mt-1 flex flex-wrap gap-x-2 gap-y-1">
              {hashtags.map((tag) => (
                <span key={tag} className="text-[15px] font-semibold text-[var(--app-brand)]">#{tag}</span>
              ))}
            </div>
          ) : null}
        </button>
        {post.location && (
          <a href={googleMapsLocationUrl(post.location)} target="_blank" rel="noopener noreferrer"
            className="mt-2 inline-flex max-w-full items-center gap-2 rounded-lg py-0.5 pr-2 text-sm text-[var(--app-muted)] transition-colors hover:bg-[var(--app-surface-soft)] hover:text-[var(--app-brand)] focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-[var(--app-brand)]">
            {/* Dùng cùng nét icon với phần chọn địa điểm, chỉ thu nhỏ để hợp với bố cục bài đăng. */}
            <span className="flex h-6 w-6 shrink-0 items-center justify-center rounded-full bg-[color-mix(in_srgb,var(--app-brand)_10%,var(--app-surface))] text-[var(--app-brand)]" aria-hidden="true">
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                <path d="M20 10c0 5-8 11-8 11S4 15 4 10a8 8 0 1 1 16 0Z" />
                <circle cx="12" cy="10" r="2.5" />
              </svg>
            </span>
            <span className="min-w-0 truncate font-medium">
              {post.location.displayName}
              {locationMeta ? <span className="font-normal text-[var(--app-muted)]"> · {locationMeta}</span> : null}
            </span>
          </a>
        )}
        {/* Media nằm ngoài nút điều hướng để controls của video có thể tương tác độc lập. */}
        <PostMediaGrid post={post} />

        {/* Thanh hành động: like, comment, repost và chia sẻ liên kết. */}
        <footer className="mt-3 flex items-center gap-6">
          <button
            className={`post-action ${liked ? 'post-action--active text-red-500' : ''}`}
            onClick={handleLike}
            disabled={likeSubmitting}
            aria-label={liked ? 'Bỏ thích bài viết' : 'Thích bài viết'}
          >
            <HeartIcon filled={liked} />
            <span className="font-normal">{formatNumber(likeCount)}</span>
          </button>
          <button
            type="button"
            className="post-action"
            onClick={() => navigate(`/posts/${post.id}`)}
            aria-label="Xem chi tiết bài viết và bình luận"
            title="Xem chi tiết bài viết"
          >
            <CommentIcon />
            <span className="font-normal">{formatNumber(commentCount)}</span>
          </button>
          {/* Repost gọi API idempotent; bài của chính mình được vô hiệu hóa theo rule Backend. */}
          <button
            className={`post-action ${reposted ? 'post-action--active text-emerald-600' : ''}`}
            onClick={handleRepost}
            disabled={isOwner || repostSubmitting}
            title={isOwner ? 'Không thể đăng lại bài viết của chính mình' : (reposted ? 'Bỏ đăng lại' : 'Đăng lại')}
          >
            <RepostIcon />
            <span className="font-normal">{formatNumber(repostCount)}</span>
          </button>
          <button className="post-action" onClick={handleCopyPostLink} title="Chia sẻ liên kết">
            <ShareIcon />
          </button>
        </footer>
      </div>

      <UserRestrictionAction
        open={restrictionConfirmOpen}
        userId={author.id}
        displayName={author.displayName}
        restricted={restrictedByMe}
        onClose={() => setRestrictionConfirmOpen(false)}
        onChanged={setRestrictedByMe}
      />

      {/* Modal chỉnh sửa bài viết */}
      <Modal
        open={editing}
        title="Chỉnh sửa bài viết"
        onClose={closeEdit}
        footer={
          <div className="flex w-full items-center gap-3">
            <Button variant="secondary" className="flex-1 !rounded-xl !h-[44px] text-[15px] font-bold" disabled={editSubmitting} onClick={closeEdit}>Hủy</Button>
            {editError && !editSource ? (
              <Button className="flex-1 !rounded-xl !h-[44px] text-[15px] font-bold" disabled={editLoading} onClick={loadEditDetail}>
                {editLoading ? 'Đang tải...' : 'Thử lại'}
              </Button>
            ) : (
              <Button className="flex-1 !rounded-xl !h-[44px] text-[15px] font-bold"
                disabled={editLoading || editSubmitting || editMediaBusy || !editSource || (!draft.trim() && editMediaSelection.totalCount === 0) || draft.length > 500}
                onClick={saveEdit}>
                {editSubmitting ? 'Đang lưu...' : 'Lưu thay đổi'}
              </Button>
            )}
          </div>
        }
        footerClassName="!border-none !pt-2 !pb-6"
      >
        {editLoading && !editSource ? (
          <LoadingState message="Đang tải chi tiết bài viết..." />
        ) : editSource ? (
          <>
            <div className="flex items-center gap-3 mb-4">
              <Avatar src={editSource.post.author?.avatarUrl ?? author.avatarUrl} name={editSource.post.author?.displayName ?? author.displayName} size="sm" />
              <p className="text-sm font-bold">{editSource.post.author?.displayName ?? author.displayName}</p>
            </div>
            <textarea
              className="app-field min-h-32 w-full resize-none rounded-xl border p-3 text-[15px] outline-none transition"
              value={draft}
              maxLength={500}
              disabled={editSubmitting}
              onChange={(event) => setDraft(event.target.value)}
            />
            <div className="mt-2 flex justify-end text-xs text-[var(--app-muted)]">
              <span>{draft.length}/500</span>
            </div>
            <div className="mt-3 rounded-xl border border-[var(--app-border)] px-3 py-2">
              <PostHashtagPicker
                value={tags || null}
                onChange={(name) => setTags(name ?? '')}
                disabled={editSubmitting}
              />
            </div>
            <EditPostMedia
              media={editSource.post.media ?? []}
              disabled={editSubmitting}
              onChange={setEditMediaSelection}
              onBusyChange={setEditMediaBusy}
            />
            <SelectedLocation location={editLocation} onRemove={() => !editSubmitting && setEditLocation(null)} />
            <button type="button" disabled={editSubmitting} onClick={() => setLocationPickerOpen((open) => !open)}
              className="mt-2 text-sm font-semibold text-[var(--app-brand)] disabled:opacity-50">
              {editLocation ? 'Thay đổi địa điểm' : 'Gắn địa điểm'}
            </button>
            {locationPickerOpen && (
              <LocationPicker onSelect={setEditLocation} onClose={() => setLocationPickerOpen(false)} />
            )}
          </>
        ) : null}
        {editError && <p className="app-error mt-3 rounded-xl p-3 text-sm">{editError}</p>}
      </Modal>

      {/* Modal xác nhận xóa hoặc xóa thành công */}
      <Modal
        open={Boolean(deleteStep)}
        title={deleteStep === 'confirm' ? "Xóa bài viết?" : ""}
        onClose={() => deleteStep === 'confirm' ? setDeleteStep(null) : executeDelete()}
        size="sm"
        footer={
          deleteStep === 'confirm' ? (
            <div className="flex w-full items-center gap-3">
              <Button variant="secondary" className="flex-1 !rounded-xl !h-[44px] text-[15px] font-bold" onClick={() => setDeleteStep(null)}>Hủy</Button>
              <Button variant="primary" className="flex-1 !rounded-xl !h-[44px] text-[15px] font-bold" onClick={confirmDelete}>Xóa bài viết</Button>
            </div>
          ) : (
            <Button className="w-full !rounded-xl !h-[44px] text-[15px] font-bold" onClick={executeDelete}>Xong</Button>
          )
        }
        footerClassName="!border-none !pt-2 !pb-6"
      >
        {deleteStep === 'confirm' ? (
          <p className="text-sm text-[var(--app-muted)] leading-relaxed">
            Bài viết sẽ không còn xuất hiện trên trang cá nhân, bảng tin và kết quả tìm kiếm. Bạn không thể hoàn tác thao tác này.
          </p>
        ) : (
          <div className="flex flex-col items-center py-2 text-center">
            <SuccessIcon />
            <h3 className="text-[16px] font-bold text-[var(--app-text)]">Đã xóa bài viết</h3>
          </div>
        )}
      </Modal>

      {/* Flow báo cáo bài viết */}
      <ReportPostFlow open={reporting} post={post} onClose={() => setReporting(false)} />
    </article>
  );
}
