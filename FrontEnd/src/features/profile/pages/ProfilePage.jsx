import { useEffect, useRef, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { Ban, Flag, MoreHorizontal } from 'lucide-react';
import Avatar from '../../../components/common/Avatar.jsx';
import Button from '../../../components/common/Button.jsx';
import Modal from '../../../components/common/Modal.jsx';
import { EmptyState } from '../../../components/common/StateBlock.jsx';
import UserRestrictionAction from '../components/UserRestrictionAction.jsx';
import ProfileReportDialog from '../components/ProfileReportDialog.jsx';
import { useApp } from '../../../contexts/AppContext.jsx';
import ContentShell from '../../../components/layout/ContentShell.jsx';
import InfinitePostList from '../../post/components/InfinitePostList.jsx';
import { useInfinitePosts } from '../../post/hooks/useInfinitePosts.js';
import PostComposer from '../../post/components/PostComposer.jsx';
import UnfollowConfirmModal from '../../../components/common/UnfollowConfirmModal.jsx';
import { socialApi } from '../../../api/index.js';
import { messagingApi } from '../../messaging/services/messagingApi.js';
import { isRequestCanceled } from '../../../api/apiError.js';
import { toFeedItemView, toPostView } from '../../post/utils/postViewModel.js';
import {
  normalizeFollowUser,
  sameUserId,
  updateFollowStateInLists,
} from '../utils/followListState.js';

function ProfilePageSkeleton({ self, onBack }) {
  // Skeleton giữ nguyên khung trang thật để chuyển route không bị co giãn hoặc nhảy bố cục.
  const header = (
    <div className="flex h-[var(--header-height)] items-center px-6">
      {!self && (
        <button
          type="button"
          onClick={onBack}
          className="-ml-2 mr-3 flex h-8 w-8 items-center justify-center rounded-full text-[var(--app-text)] transition hover:bg-[var(--app-surface-soft)]"
          aria-label="Quay lại"
        >
          <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round">
            <path d="M19 12H5M12 19l-7-7 7-7" />
          </svg>
        </button>
      )}
      <div className="h-5 w-36 animate-pulse rounded-md bg-[var(--app-border)]" />
    </div>
  );

  return (
    <ContentShell header={header}>
      <div className="animate-pulse">
        <div className="px-6 pb-0 pt-6">
          <div className="flex items-start justify-between">
            <div className="flex-1 space-y-2 pt-1">
              <div className="h-7 w-44 rounded-lg bg-[var(--app-border)]" />
              <div className="h-4 w-28 rounded-md bg-[var(--app-border)]" />
            </div>
            <div className="h-[84px] w-[84px] shrink-0 rounded-full bg-[var(--app-border)]" />
          </div>

          <div className="mt-5 space-y-2">
            <div className="h-4 w-full rounded-md bg-[var(--app-border)]" />
            <div className="h-4 w-3/5 rounded-md bg-[var(--app-border)]" />
          </div>

          <div className="mt-5 flex gap-5">
            <div className="h-4 w-28 rounded-md bg-[var(--app-border)]" />
            <div className="h-4 w-28 rounded-md bg-[var(--app-border)]" />
          </div>

          <div className="mt-6 h-9 w-full rounded-xl bg-[var(--app-border)]" />

          <div className="mt-4 grid grid-cols-3 border-b border-[var(--app-border)]">
            <div className="mx-auto mb-3 h-4 w-16 rounded-md bg-[var(--app-border)]" />
            <div className="mx-auto mb-3 h-4 w-16 rounded-md bg-[var(--app-border)]" />
            <div className="mx-auto mb-3 h-4 w-16 rounded-md bg-[var(--app-border)]" />
          </div>
        </div>

        <div className="border-b border-[var(--app-border)] px-6 py-5">
          <div className="flex gap-3">
            <div className="h-10 w-10 shrink-0 rounded-full bg-[var(--app-border)]" />
            <div className="flex-1 space-y-3">
              <div className="h-4 w-32 rounded-md bg-[var(--app-border)]" />
              <div className="h-4 w-full rounded-md bg-[var(--app-border)]" />
              <div className="h-4 w-4/5 rounded-md bg-[var(--app-border)]" />
            </div>
          </div>
        </div>
      </div>
    </ContentShell>
  );
}

export default function ProfilePage({ self = false }) {
  const { userId } = useParams();
  const navigate = useNavigate();
  const {
    currentUserId, toggleFollow, applyUserBlock, showToast, updateProfile, syncCurrentUserProfile,
  } = useApp();
  const [editing, setEditing] = useState(false);
  const [savingProfile, setSavingProfile] = useState(false);
  const [composerOpen, setComposerOpen] = useState(false);
  const [followModal, setFollowModal] = useState(null);
  const [modalUsers, setModalUsers] = useState({ followers: [], following: [] });
  const [activeTab, setActiveTab] = useState('threads');
  const [unfollowTarget, setUnfollowTarget] = useState(null);
  const [followPendingId, setFollowPendingId] = useState(null);
  const [blockConfirmOpen, setBlockConfirmOpen] = useState(false);
  const [reportDialogOpen, setReportDialogOpen] = useState(false);
  const [startingMessage, setStartingMessage] = useState(false);

  async function handleMessageClick() {
    if (startingMessage || !profile?.id) return;
    setStartingMessage(true);
    try {
      const result = await messagingApi.openDirectConversation(profile.id);
      if (result?.conversationId) {
        navigate(`/messages/${result.conversationId}`, {
          state: {
            otherUser: {
              userId: profile.id,
              displayName: profile.displayName,
              avatarUrl: profile.avatarUrl,
            },
          },
        });
      }
    } catch (err) {
      showToast(err.message || 'Không thể mở tin nhắn. Vui lòng thử lại.');
    } finally {
      setStartingMessage(false);
    }
  }
  const [profileOptionsOpen, setProfileOptionsOpen] = useState(false);
  const [blocking, setBlocking] = useState(false);
  const [error, setError] = useState('');
  const [profile, setProfile] = useState(null);
  const [loadedProfileKey, setLoadedProfileKey] = useState(null);
  const profileKey = self ? 'me' : String(userId);
  const profileUserId = self ? currentUserId : userId;
  const postState = useInfinitePosts({
    cacheKey: `profile-posts:${profileUserId ?? 'unknown'}`,
    request: (params) => socialApi.getUserPosts(profileUserId, params),
    normalizePost: toPostView,
    enabled: Boolean(profileUserId),
    active: activeTab === 'threads',
  });
  const repostState = useInfinitePosts({
    cacheKey: `profile-reposts:${profileUserId ?? 'unknown'}`,
    request: (params) => socialApi.getUserReposts(profileUserId, params),
    normalizePost: toFeedItemView,
    enabled: Boolean(profileUserId),
    active: activeTab === 'reposts',
  });
  const [draft, setDraft] = useState({ displayName: '', bio: '', avatarUrl: '', dateOfBirth: '' });
  const profileOptionsRef = useRef(null);

  useEffect(() => {
    if (!profileOptionsOpen) return undefined;
    const closeOptions = (event) => {
      if (!profileOptionsRef.current?.contains(event.target)) setProfileOptionsOpen(false);
    };
    document.addEventListener('pointerdown', closeOptions);
    return () => document.removeEventListener('pointerdown', closeOptions);
  }, [profileOptionsOpen]);

  useEffect(() => {
    const controller = new AbortController();

    const request = self
      ? socialApi.getMyProfile(controller.signal)
      : socialApi.getProfile(userId, controller.signal);

    request
      .then((response) => {
        // Chuẩn hóa contract Backend về view model đang dùng trong trang hồ sơ.
        const loadedProfile = {
          ...response,
          id: response.userId,
          birthDate: response.dateOfBirth ?? null,
          status: 'ACTIVE',
        };
        setProfile(loadedProfile);
        setLoadedProfileKey(profileKey);
        setError('');
        setDraft({
          displayName: loadedProfile.displayName ?? '',
          bio: loadedProfile.bio ?? '',
          avatarUrl: loadedProfile.avatarUrl ?? '',
          dateOfBirth: loadedProfile.birthDate ?? '',
        });
      })
      .catch((requestError) => {
        if (!isRequestCanceled(requestError)) {
          setProfile(null);
          setLoadedProfileKey(profileKey);
          setError(requestError.message);
        }
      });

    return () => controller.abort();
  }, [profileKey, self, userId]);

  if (loadedProfileKey !== profileKey) {
    return <ProfilePageSkeleton self={self} onBack={() => navigate(-1)} />;
  }

  if (!profile) {
    return <EmptyState title="Không tìm thấy hồ sơ" description={error || 'Hồ sơ không tồn tại hoặc không khả dụng.'} actionLabel="Về feed" onAction={() => navigate('/feed/for-you')} />;
  }

  const isSelf = String(profile.id) === String(currentUserId);
  const isFollowing = profile.followedByCurrentUser;

  // Hồ sơ công khai chỉ hiển thị tên hiển thị, không suy diễn username từ email hoặc userId.
  const handle = profile.displayName;

  function openEdit() {
    setDraft({ displayName: profile.displayName, bio: profile.bio, avatarUrl: profile.avatarUrl, dateOfBirth: profile.birthDate ?? '' });
    setError('');
    setEditing(true);
  }

  async function saveProfile() {
    if (savingProfile) return;
    setSavingProfile(true);
    setError('');
    try {
      await updateProfile(draft);
      setProfile((current) => ({
        ...current,
        displayName: draft.displayName.trim(),
        bio: draft.bio?.trim() ?? '',
        birthDate: draft.dateOfBirth,
        dateOfBirth: draft.dateOfBirth,
      }));
      setEditing(false);
      showToast('Đã chỉnh sửa thông tin người dùng thành công.');
    } catch (requestError) {
      setError(requestError.message);
    } finally {
      setSavingProfile(false);
    }
  }

  async function openFollowModal(type) {
    setFollowModal(type);
    try {
      const [followerItems, followingItems] = await Promise.all([
        socialApi.getFollowers(profile.id), socialApi.getFollowing(profile.id),
      ]);
      setModalUsers({
        followers: followerItems.map(normalizeFollowUser),
        following: followingItems.map(normalizeFollowUser),
      });
      setError('');
    } catch (requestError) {
      setError(requestError.message);
    }
  }

  async function changeAvatar(event) {
    const file = event.target.files?.[0];
    event.target.value = '';
    if (!file) return;
    try {
      const response = await socialApi.uploadAvatar(file);
      // Đồng bộ avatar mới cho Feed, modal tạo bài và composer bình luận trong cùng phiên.
      syncCurrentUserProfile({ avatarUrl: response.avatarUrl });
      setDraft((current) => ({ ...current, avatarUrl: response.avatarUrl }));
      setProfile((current) => ({ ...current, avatarUrl: response.avatarUrl }));
      setError('');
    } catch (requestError) {
      setError(requestError.message);
    }
  }

  async function removeAvatar() {
    try {
      const response = await socialApi.deleteAvatar();
      // Avatar bị xóa cũng phải được phản ánh ngay tại mọi composer đang dùng currentUser.
      syncCurrentUserProfile({ avatarUrl: response.avatarUrl || '' });
      setDraft((current) => ({ ...current, avatarUrl: response.avatarUrl || '' }));
      setProfile((current) => ({ ...current, avatarUrl: response.avatarUrl || '' }));
      setError('');
    } catch (requestError) {
      setError(requestError.message);
    }
  }

  async function handleFollowClick(userTarget, isCurrentlyFollowing) {
    if (followPendingId !== null) return;
    if (isCurrentlyFollowing) {
      setUnfollowTarget(userTarget);
    } else {
      setFollowPendingId(userTarget.id);
      try {
        const response = await toggleFollow(userTarget.id, false);
        setModalUsers((current) => updateFollowStateInLists(
          current,
          userTarget.id,
          response.followedByCurrentUser,
        ));
        if (sameUserId(userTarget.id, profile.id)) {
          setProfile((current) => ({
            ...current,
            followedByCurrentUser: response.followedByCurrentUser,
            followerCount: response.followedByCurrentUser
              ? current.followerCount + 1
              : current.followerCount,
          }));
        }
        setError('');
      } catch (requestError) {
        setError(requestError.message);
      } finally {
        setFollowPendingId(null);
      }
    }
  }

  async function confirmUnfollow() {
    if (unfollowTarget && followPendingId === null) {
      const target = unfollowTarget;
      setFollowPendingId(target.id);
      try {
        const response = await toggleFollow(target.id, true);
        setModalUsers((current) => updateFollowStateInLists(
          current,
          target.id,
          response.followedByCurrentUser,
        ));
        if (sameUserId(target.id, profile.id)) {
          setProfile((current) => ({
            ...current,
            followedByCurrentUser: response.followedByCurrentUser,
            followerCount: response.followedByCurrentUser
              ? current.followerCount
              : Math.max(0, current.followerCount - 1),
          }));
        }
        setUnfollowTarget(null);
        setError('');
      } catch (requestError) {
        setError(requestError.message);
      } finally {
        setFollowPendingId(null);
      }
    }
  }

  async function confirmBlock() {
    if (blocking) return;
    setBlocking(true);
    try {
      // Backend tự lấy blocker từ JWT; Frontend chỉ truyền tài khoản đích.
      await socialApi.blockUser(profile.id);
      applyUserBlock(profile.id);
      showToast(`Đã chặn ${profile.displayName}.`);
      setBlockConfirmOpen(false);
      navigate('/feed/for-you', { replace: true });
    } catch (requestError) {
      setError(requestError.message);
    } finally {
      setBlocking(false);
    }
  }

  const profileHeaderNav = (
    <div className="flex h-[var(--header-height)] items-center justify-between px-6">
      <div className="flex items-center gap-2">
        {!isSelf && (
          <button
            className="flex h-8 w-8 items-center justify-center rounded-full text-[var(--app-text)] transition hover:bg-[var(--app-surface-soft)] -ml-2"
            onClick={() => navigate(-1)}
            aria-label="Quay lại"
          >
            <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
              <line x1="19" x2="5" y1="12" y2="12" />
              <polyline points="12 19 5 12 12 5" />
            </svg>
          </button>
        )}
        <h2 className="text-[17px] font-bold text-[var(--app-text)]">{!isSelf ? handle.replace('@', '') : profile.displayName}</h2>
      </div>
      <div className="flex items-center gap-4 text-[var(--app-text)]">
        <button aria-label="Tìm kiếm" onClick={() => navigate('/search')}>
          <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><circle cx="11" cy="11" r="8"/><path d="m21 21-4.3-4.3"/></svg>
        </button>
      </div>
    </div>
  );

  return (
    <>
      <ContentShell header={profileHeaderNav}>
        <div className="px-6 pt-6 pb-0">
          <div className="flex items-start justify-between">
            <div className="flex-1 pr-4">
              <h1 className="text-2xl font-bold text-[var(--app-text)]">{profile.displayName}</h1>
              <p className="text-[15px] text-[var(--app-muted)] mt-0.5">{handle}</p>
            </div>
            <div className="shrink-0">
              <Avatar src={profile.avatarUrl} name={profile.displayName} size="lg" viewable className="!w-[84px] !h-[84px] text-3xl" />
            </div>
          </div>

          <p className="mt-3 text-[15px] text-[var(--app-text)] leading-relaxed whitespace-pre-wrap">{profile.bio || 'Chưa có giới thiệu.'}</p>
          
          <div className="mt-4 flex items-center justify-between text-[15px] text-[var(--app-muted)]">
            <div className="flex gap-4">
              <button className="hover:underline flex gap-1.5" onClick={() => openFollowModal('followers')}>
                <span className="font-semibold text-[var(--app-text)]">{profile.followerCount}</span> người theo dõi
              </button>
              <button className="hover:underline flex gap-1.5" onClick={() => openFollowModal('following')}>
                <span className="font-semibold text-[var(--app-text)]">{profile.followingCount}</span> đang theo dõi
              </button>
            </div>
            {!isSelf ? (
              <div ref={profileOptionsRef} className="relative">
                <button
                  type="button"
                  className={`flex h-9 w-9 items-center justify-center rounded-full border transition ${
                    profileOptionsOpen
                      ? 'border-[var(--app-text)] bg-[var(--app-surface-soft)]'
                      : 'border-[var(--app-border-strong)] hover:bg-[var(--app-surface-soft)]'
                  }`}
                  aria-label="Tùy chọn tài khoản"
                  aria-expanded={profileOptionsOpen}
                  onClick={() => setProfileOptionsOpen((open) => !open)}
                >
                  <MoreHorizontal size={21} strokeWidth={2} />
                </button>
                {profileOptionsOpen ? (
                  <div className="post-menu-dropdown !top-11 !z-50">
                    <UserRestrictionAction
                      userId={profile.id}
                      displayName={profile.displayName}
                      initialRestricted={profile.restrictedByMe}
                      blocked={profile.blockedByMe}
                      onTrigger={() => setProfileOptionsOpen(false)}
                      onChanged={(restrictedByMe) => setProfile((current) => ({ ...current, restrictedByMe }))}
                    />
                    <button
                      type="button"
                      onClick={() => {
                        setProfileOptionsOpen(false);
                        setReportDialogOpen(true);
                      }}
                    >
                      <span>Báo cáo</span>
                      <Flag size={16} strokeWidth={2} aria-hidden="true" />
                    </button>
                    <button
                      type="button"
                      className="danger-item"
                      onClick={() => {
                        setProfileOptionsOpen(false);
                        setBlockConfirmOpen(true);
                      }}
                    >
                      <span>Chặn</span>
                      <Ban size={16} strokeWidth={2} aria-hidden="true" />
                    </button>
                  </div>
                ) : null}
              </div>
            ) : null}
          </div>

          <div className="mt-6">
            {isSelf ? (
              <Button variant="secondary" className="w-full !rounded-xl !font-semibold !border-[var(--app-border-strong)] !h-[36px] text-[15px] text-[var(--app-text)]" onClick={openEdit}>
                Chỉnh sửa trang cá nhân
              </Button>
            ) : (
              <div className="flex gap-3">
                <Button
                  className="flex-1 !rounded-xl !font-semibold !h-[36px] text-[15px]"
                  disabled={sameUserId(followPendingId, profile.id)}
                  onClick={() => handleFollowClick(profile, isFollowing)}
                >
                  {sameUserId(followPendingId, profile.id)
                    ? 'Đang xử lý...'
                    : (isFollowing ? 'Bỏ theo dõi' : 'Theo dõi')}
                </Button>
                <Button
                  variant="secondary"
                  className="flex-1 !rounded-xl !font-semibold !border-[var(--app-border-strong)] !h-[36px] text-[15px] text-[var(--app-text)]"
                  disabled={startingMessage}
                  onClick={handleMessageClick}
                >
                  {startingMessage ? 'Đang mở...' : 'Nhắn tin'}
                </Button>
              </div>
            )}
          </div>

          <div className="mt-4 flex text-[15px] font-semibold text-[var(--app-muted)] w-full">
            <button 
              onClick={() => setActiveTab('threads')}
              className={`flex-1 pb-3 text-center transition ${activeTab === 'threads' ? 'border-b-[1.5px] border-[var(--app-text)] text-[var(--app-text)]' : 'border-b-[1px] border-[var(--app-border)] hover:text-[var(--app-text)]'}`}
            >
              Bài đăng
            </button>
            <button 
              onClick={() => setActiveTab('replies')}
              className={`flex-1 pb-3 text-center transition ${activeTab === 'replies' ? 'border-b-[1.5px] border-[var(--app-text)] text-[var(--app-text)]' : 'border-b-[1px] border-[var(--app-border)] hover:text-[var(--app-text)]'}`}
            >
              Câu trả lời
            </button>
            <button 
              onClick={() => setActiveTab('reposts')}
              className={`flex-1 pb-3 text-center transition ${activeTab === 'reposts' ? 'border-b-[1.5px] border-[var(--app-text)] text-[var(--app-text)]' : 'border-b-[1px] border-[var(--app-border)] hover:text-[var(--app-text)]'}`}
            >
              Bài đăng lại
            </button>
          </div>
        </div>
        
        {isSelf && (
          <div className="flex items-center gap-4 border-b border-[var(--app-border)] px-6 pb-4 pt-4">
            <Avatar src={profile.avatarUrl} name={profile.displayName} size="sm" className="!w-9 !h-9 text-sm" />
            <button className="flex-1 text-left text-[15px] text-[var(--app-muted)]" onClick={() => setComposerOpen(true)}>
              Có gì mới?
            </button>
            <Button variant="secondary" className="!h-[36px] !w-[84px] !p-0 text-[15px] !font-semibold !rounded-[10px] !border-[var(--app-border-strong)]" size="sm" onClick={() => setComposerOpen(true)}>
              Đăng
            </Button>
          </div>
        )}

        <div className="min-h-[50vh]">
          {activeTab === 'threads' && (
            <InfinitePostList
              {...postState}
              errorTitle="Không thể tải bài viết"
              emptyTitle="Chưa có bài viết"
              emptyDescription="Các bài đã đăng sẽ hiển thị tại đây."
            />
          )}
          {activeTab === 'replies' && (
            <EmptyState title="Chưa có câu trả lời" description="Các bình luận của bạn sẽ xuất hiện ở đây." />
          )}
          {activeTab === 'reposts' && (
            <InfinitePostList
              {...repostState}
              showRepostAttribution={false}
              onRepostChange={(postId, reposted) => {
                if (!reposted && isSelf) repostState.removePost(postId);
              }}
              errorTitle="Không thể tải bài đăng lại"
              emptyTitle="Chưa có bài đăng lại"
              emptyDescription="Những bài đã đăng lại sẽ xuất hiện tại đây."
            />
          )}
        </div>
      </ContentShell>

      <Modal
        open={blockConfirmOpen}
        title="Chặn người dùng này?"
        onClose={() => !blocking && setBlockConfirmOpen(false)}
        footer={<>
          <Button variant="secondary" disabled={blocking} onClick={() => setBlockConfirmOpen(false)}>Hủy</Button>
          <Button disabled={blocking} onClick={confirmBlock}>{blocking ? 'Đang xử lý...' : 'Chặn'}</Button>
        </>}
      >
        Hai bạn sẽ không thể xem hồ sơ, bài viết hoặc tương tác với nhau. Quan hệ theo dõi hiện tại sẽ bị hủy.
      </Modal>

      <ProfileReportDialog
        open={reportDialogOpen}
        user={profile}
        onClose={() => setReportDialogOpen(false)}
      />

      <Modal
        open={editing}
        onClose={() => !savingProfile && setEditing(false)}
        customHeader={
          <header className="flex shrink-0 items-start justify-between border-b border-[var(--app-border)] px-6 py-4">
            <div>
              <h2 className="text-[17px] font-bold text-[var(--app-text)]">Chỉnh sửa hồ sơ</h2>
              <p className="text-[13px] text-[var(--app-muted)]">Cập nhật thông tin hồ sơ cá nhân của bạn</p>
            </div>
            <button 
              className="flex h-8 w-8 items-center justify-center rounded-full text-[var(--app-muted)] transition hover:bg-[var(--app-surface-soft)] disabled:opacity-50" 
              onClick={() => setEditing(false)} 
              disabled={savingProfile}
              aria-label="Dong modal"
            >
              <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><path d="M18 6 6 18"/><path d="m6 6 12 12"/></svg>
            </button>
          </header>
        }
        footer={
          <Button 
            disabled={savingProfile || !draft.displayName.trim() || !draft.dateOfBirth}
            loading={savingProfile}
            loadingLabel="Đang xử lý..."
            onClick={saveProfile}
            className="w-full !bg-[var(--app-active)] !text-[var(--app-surface)] hover:opacity-80 !rounded-xl !h-[50px] !font-bold text-[16px]"
          >
            Xong
          </Button>
        }
        footerClassName="!border-none !pt-2 !pb-6"
      >
        {error && <p className="app-error mb-4 rounded-xl p-3 text-sm">{error}</p>}
        <div className="flex flex-col items-center mt-2 mb-6">
          <Avatar src={draft.avatarUrl} name={draft.displayName} size="lg" className="!w-[56px] !h-[56px] text-2xl" />
          <label className={`mt-2 text-[14px] font-semibold text-blue-600 hover:underline ${savingProfile ? 'pointer-events-none opacity-50' : 'cursor-pointer'}`}>Thay đổi ảnh
            <input type="file" accept="image/jpeg,image/png,image/webp" hidden onChange={changeAvatar} disabled={savingProfile} />
          </label>
          {draft.avatarUrl && <button onClick={removeAvatar} disabled={savingProfile} className="mt-1 text-xs text-red-600 disabled:opacity-50">Xóa ảnh</button>}
        </div>

        <div className="mb-5">
          <label className="text-[15px] font-semibold text-[var(--app-text)] mb-1.5 block">Ngày sinh</label>
          <input type="date" required value={draft.dateOfBirth} disabled={savingProfile} onChange={(event) => setDraft({ ...draft, dateOfBirth: event.target.value })}
            className="app-field w-full rounded-xl border px-3.5 py-3 text-[15px] outline-none transition disabled:opacity-50" />
        </div>

        <div className="mb-5">
          <div className="flex justify-between items-center mb-1.5">
            <label className="text-[15px] font-semibold text-[var(--app-text)]">Tên hiển thị</label>
            <span className="text-[13px] text-[var(--app-muted)]">{draft.displayName.length}/50</span>
          </div>
          <input 
            className="app-field w-full rounded-xl border px-3.5 py-3 text-[15px] outline-none transition disabled:opacity-50" 
            value={draft.displayName} 
            maxLength={50}
            disabled={savingProfile}
            onChange={(e) => setDraft({ ...draft, displayName: e.target.value })} 
          />
          {!draft.displayName.trim() && <p className="text-red-500 text-[13px] mt-1.5">Tên hiển thị không được để trống</p>}
        </div>

        <div className="mb-5">
          <label className="text-[15px] font-semibold text-[var(--app-text)] mb-1.5 block">Tên người dùng</label>
          <input 
            className="w-full rounded-xl border border-transparent bg-[var(--app-surface-soft)] text-[var(--app-muted)] px-3.5 py-3 text-[15px] outline-none cursor-not-allowed" 
            value={handle.replace('@', '')} 
            disabled 
          />
          <p className="text-[var(--app-muted)] text-[13px] mt-1.5">Tên người dùng hiện chưa thể chỉnh sửa</p>
        </div>

        <div className="mb-6">
          <div className="flex justify-between items-center mb-1.5">
            <label className="text-[15px] font-semibold text-[var(--app-text)]">Tiểu sử</label>
            <span className="text-[13px] text-[var(--app-muted)]">{(draft.bio || '').length}/160</span>
          </div>
          <textarea 
            className="app-field w-full min-h-[96px] resize-none rounded-xl border px-3.5 py-3 text-[15px] outline-none transition disabled:opacity-50" 
            placeholder="Viết vài dòng giới thiệu về bạn"
            value={draft.bio} 
            maxLength={160}
            disabled={savingProfile}
            onChange={(e) => setDraft({ ...draft, bio: e.target.value })} 
          />
        </div>

        <div className="mb-2">
          <div className="flex justify-between items-center mb-1">
            <span className="text-[15px] font-semibold text-[var(--app-text)]">Quyền riêng tư của trang cá nhân</span>
            <button disabled={savingProfile} className="flex items-center gap-1 text-[15px] text-[var(--app-muted)] hover:text-[var(--app-text)] transition disabled:opacity-50">
              Riêng tư
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><path d="m9 18 6-6-6-6"/></svg>
            </button>
          </div>
          <p className="text-[13px] text-[var(--app-muted)] leading-relaxed">
            Nếu bạn chuyển sang chế độ công khai, bất kỳ ai cũng có thể nhìn thấy bài đăng và bài đăng trả lời của bạn.
          </p>
        </div>
      </Modal>

      <Modal 
        open={Boolean(followModal)} 
        onClose={() => setFollowModal(null)}
        customHeader={
          <header className="flex flex-col border-b border-[var(--app-border)] pt-2 relative">
            <div className="flex px-4">
              <button 
                className={`flex-1 flex flex-col items-center justify-center pb-3 pt-2 ${followModal === 'followers' ? 'border-b-[1.5px] border-[var(--app-text)] text-[var(--app-text)]' : 'text-[var(--app-muted)]'}`}
                onClick={() => setFollowModal('followers')}
              >
                <span className="text-[15px] font-bold">Người theo dõi</span>
                <span className="text-[13px]">{profile.followerCount}</span>
              </button>
              <button 
                className={`flex-1 flex flex-col items-center justify-center pb-3 pt-2 ${followModal === 'following' ? 'border-b-[1.5px] border-[var(--app-text)] text-[var(--app-text)]' : 'text-[var(--app-muted)]'}`}
                onClick={() => setFollowModal('following')}
              >
                <span className="text-[15px] font-bold">Đang theo dõi</span>
                <span className="text-[13px]">{profile.followingCount}</span>
              </button>
            </div>
          </header>
        }
      >
        <div className="flex flex-col">
          {(followModal === 'followers' ? modalUsers.followers : modalUsers.following).map((user, index) => {
            const isUserFollowing = user.followedByCurrentUser;
            const followPending = sameUserId(followPendingId, user.id);
            // Danh sách follow dùng displayName theo contract và không phụ thuộc kiểu dữ liệu của userId.
            const userHandle = user.displayName;
            
            return (
              <div key={user.id} className={`flex w-full items-center justify-between py-3 ${index !== (followModal === 'followers' ? modalUsers.followers : modalUsers.following).length - 1 ? 'border-b border-[var(--app-border)]' : ''}`}>
                <div className="flex items-center gap-3 cursor-pointer" onClick={() => { setFollowModal(null); navigate(user.id === currentUserId ? '/profile/me' : `/profile/${user.id}`) }}>
                  <Avatar src={user.avatarUrl} name={user.displayName} size="md" className="!w-10 !h-10 text-sm" />
                  <div className="flex flex-col">
                    <span className="text-[14px] font-bold text-[var(--app-text)]">{userHandle.replace('@', '')}</span>
                    <span className="text-[14px] text-[var(--app-muted)]">{user.displayName}</span>
                  </div>
                </div>
                {!sameUserId(user.id, currentUserId) && (
                  <Button 
                    variant={isUserFollowing ? "secondary" : "primary"} 
                    className={`!rounded-xl !h-[34px] px-5 font-bold text-[14px] ${isUserFollowing ? '!border-[var(--app-border-strong)] text-[var(--app-text)]' : '!bg-[var(--app-active)] !text-[var(--app-surface)] hover:opacity-80'}`}
                    disabled={followPending}
                    onClick={() => handleFollowClick(user, isUserFollowing)}
                  >
                    {followPending ? 'Đang xử lý...' : (isUserFollowing ? 'Đang theo dõi' : 'Theo dõi')}
                  </Button>
                )}
              </div>
            )
          })}
          {!(followModal === 'followers' ? modalUsers.followers : modalUsers.following).length ? <p className="py-6 text-center text-[14px] text-[var(--app-muted)]">Danh sách trống.</p> : null}
        </div>
      </Modal>

      <UnfollowConfirmModal 
        open={Boolean(unfollowTarget)}
        user={unfollowTarget}
        busy={followPendingId !== null}
        onClose={() => followPendingId === null && setUnfollowTarget(null)}
        onConfirm={confirmUnfollow}
      />

      <PostComposer mode={composerOpen ? 'modal' : null} onClose={() => setComposerOpen(false)} />
    </>
  );
}
