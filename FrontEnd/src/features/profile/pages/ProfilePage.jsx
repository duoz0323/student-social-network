import { useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import Avatar from '../../../components/common/Avatar.jsx';
import Button from '../../../components/common/Button.jsx';
import Modal from '../../../components/common/Modal.jsx';
import { EmptyState } from '../../../components/common/StateBlock.jsx';
import { useApp } from '../../../contexts/AppContext.jsx';
import ContentShell from '../../../components/layout/ContentShell.jsx';
import PostCard from '../../post/components/PostCard.jsx';
import PostComposer from '../../post/components/PostComposer.jsx';
import UnfollowConfirmModal from '../../../components/common/UnfollowConfirmModal.jsx';

export default function ProfilePage({ self = false }) {
  const { userId } = useParams();
  const navigate = useNavigate();
  const { currentUser, currentUserId, getUserById, data, toggleFollow, updateProfile } = useApp();
  const [editing, setEditing] = useState(false);
  const [composerOpen, setComposerOpen] = useState(false);
  const [followModal, setFollowModal] = useState(null);
  const [modalUsers, setModalUsers] = useState({ followers: [], following: [] });
  const [activeTab, setActiveTab] = useState('threads');
  const [unfollowTarget, setUnfollowTarget] = useState(null);
  const profile = self ? currentUser : getUserById(userId);
  const [draft, setDraft] = useState({ displayName: currentUser?.displayName ?? '', bio: currentUser?.bio ?? '', avatarUrl: currentUser?.avatarUrl ?? '' });

  if (!profile || profile.status === 'BLOCKED') {
    return <EmptyState title="Khong tim thay ho so" description="Ho so khong ton tai hoac dang bi khoa." actionLabel="Ve feed" onAction={() => navigate('/feed/for-you')} />;
  }

  const isSelf = profile.id === currentUserId;
  const isFollowing = data.follows.some((follow) => follow.followerId === currentUserId && follow.followingId === profile.id);
  const posts = data.posts.filter((post) => post.authorId === profile.id && post.status === 'PUBLISHED');
  const followers = data.follows.filter((follow) => follow.followingId === profile.id).map((follow) => getUserById(follow.followerId)).filter(Boolean);
  const following = data.follows.filter((follow) => follow.followerId === profile.id).map((follow) => getUserById(follow.followingId)).filter(Boolean);

  const handle = profile.email ? `@${profile.email.split('@')[0]}` : `@user${profile.id.slice(-4)}`;

  function openEdit() {
    setDraft({ displayName: profile.displayName, bio: profile.bio, avatarUrl: profile.avatarUrl });
    setEditing(true);
  }

  function saveProfile() {
    updateProfile(draft);
    setEditing(false);
  }

  function openFollowModal(type) {
    if (!followModal) {
      setModalUsers({ followers, following });
    }
    setFollowModal(type);
  }

  function handleFollowClick(userTarget, isCurrentlyFollowing) {
    if (isCurrentlyFollowing) {
      setUnfollowTarget(userTarget);
    } else {
      toggleFollow(userTarget.id);
    }
  }

  function confirmUnfollow() {
    if (unfollowTarget) {
      toggleFollow(unfollowTarget.id);
      setUnfollowTarget(null);
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
        <button aria-label="Tùy chọn">
          <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><circle cx="12" cy="12" r="1"/><circle cx="19" cy="12" r="1"/><circle cx="5" cy="12" r="1"/></svg>
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
              <Avatar src={profile.avatarUrl} name={profile.displayName} size="lg" className="!w-[84px] !h-[84px] text-3xl" />
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
            
            {!isSelf && (
              <div className="flex items-center gap-3">
                <button className="flex h-[36px] w-[36px] items-center justify-center rounded-full text-[var(--app-text)] transition hover:bg-[var(--app-surface-soft)]" aria-label="Thông báo">
                  <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round"><path d="M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9"></path><path d="M13.73 21a2 2 0 0 1-3.46 0"></path></svg>
                </button>
                <button className="flex h-[36px] w-[36px] items-center justify-center rounded-full text-[var(--app-text)] transition hover:bg-[var(--app-surface-soft)] border border-[var(--app-border)]" aria-label="Tùy chọn">
                  <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><circle cx="12" cy="12" r="1.25"></circle><circle cx="19" cy="12" r="1.25"></circle><circle cx="5" cy="12" r="1.25"></circle></svg>
                </button>
              </div>
            )}
          </div>

          <div className="mt-6">
            {isSelf ? (
              <Button variant="secondary" className="w-full !rounded-xl !font-semibold !border-[var(--app-border-strong)] !h-[36px] text-[15px] text-[var(--app-text)]" onClick={openEdit}>
                Chỉnh sửa trang cá nhân
              </Button>
            ) : (
              <Button className="w-full !rounded-xl !font-semibold !h-[36px] text-[15px]" onClick={() => handleFollowClick(profile, isFollowing)}>
                {isFollowing ? 'Bỏ theo dõi' : 'Theo dõi'}
              </Button>
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
            <Avatar src={currentUser.avatarUrl} name={currentUser.displayName} size="sm" className="!w-9 !h-9 text-sm" />
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
            posts.length ? posts.map((post) => <PostCard key={post.id} post={post} />) : <EmptyState title="Chưa có bài viết" description="Các bài đã đăng sẽ hiển thị tại đây." />
          )}
          {activeTab === 'replies' && (
            <EmptyState title="Chưa có câu trả lời" description="Các bình luận của bạn sẽ xuất hiện ở đây." />
          )}
          {activeTab === 'reposts' && (
            <EmptyState title="Chưa có bài đăng lại" description="Những bài bạn đăng lại sẽ nằm ở đây." />
          )}
        </div>
      </ContentShell>

      <Modal
        open={editing}
        onClose={() => setEditing(false)}
        customHeader={
          <header className="flex shrink-0 items-start justify-between border-b border-[var(--app-border)] px-6 py-4">
            <div>
              <h2 className="text-[17px] font-bold text-[var(--app-text)]">Chỉnh sửa hồ sơ</h2>
              <p className="text-[13px] text-[var(--app-muted)]">Cập nhật thông tin hồ sơ cá nhân của bạn</p>
            </div>
            <button className="flex h-8 w-8 items-center justify-center rounded-full text-[var(--app-muted)] transition hover:bg-[var(--app-surface-soft)]" onClick={() => setEditing(false)} aria-label="Dong modal">
              <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><path d="M18 6 6 18"/><path d="m6 6 12 12"/></svg>
            </button>
          </header>
        }
        footer={
          <Button 
            disabled={!draft.displayName.trim()} 
            onClick={saveProfile}
            className="w-full !bg-[var(--app-active)] !text-[var(--app-surface)] hover:opacity-80 !rounded-xl !h-[50px] !font-bold text-[16px]"
          >
            Xong
          </Button>
        }
        footerClassName="!border-none !pt-2 !pb-6"
      >
        <div className="flex flex-col items-center mt-2 mb-6">
          <Avatar src={draft.avatarUrl} name={draft.displayName} size="lg" className="!w-[56px] !h-[56px] text-2xl" />
          <button className="mt-2 text-[14px] font-semibold text-blue-600 hover:underline">Thay đổi ảnh</button>
        </div>

        <div className="mb-5">
          <div className="flex justify-between items-center mb-1.5">
            <label className="text-[15px] font-semibold text-[var(--app-text)]">Tên hiển thị</label>
            <span className="text-[13px] text-[var(--app-muted)]">{draft.displayName.length}/50</span>
          </div>
          <input 
            className="w-full rounded-xl border border-[var(--app-border)] px-3.5 py-3 text-[15px] outline-none focus:border-zinc-400" 
            value={draft.displayName} 
            maxLength={50}
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
            className="w-full rounded-xl border border-[var(--app-border)] px-3.5 py-3 text-[15px] outline-none focus:border-zinc-400 min-h-[96px] resize-none" 
            placeholder="Viết vài dòng giới thiệu về bạn"
            value={draft.bio} 
            maxLength={160}
            onChange={(e) => setDraft({ ...draft, bio: e.target.value })} 
          />
        </div>

        <div className="mb-2">
          <div className="flex justify-between items-center mb-1">
            <span className="text-[15px] font-semibold text-[var(--app-text)]">Quyền riêng tư của trang cá nhân</span>
            <button className="flex items-center gap-1 text-[15px] text-[var(--app-muted)] hover:text-[var(--app-text)] transition">
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
            const isUserFollowing = data.follows.some(f => f.followerId === currentUserId && f.followingId === user.id);
            const userHandle = user.email ? `@${user.email.split('@')[0]}` : `@user${user.id.slice(-4)}`;
            
            return (
              <div key={user.id} className={`flex w-full items-center justify-between py-3 ${index !== (followModal === 'followers' ? modalUsers.followers : modalUsers.following).length - 1 ? 'border-b border-[var(--app-border)]' : ''}`}>
                <div className="flex items-center gap-3 cursor-pointer" onClick={() => { setFollowModal(null); navigate(user.id === currentUserId ? '/profile/me' : `/profile/${user.id}`) }}>
                  <Avatar src={user.avatarUrl} name={user.displayName} size="md" className="!w-10 !h-10 text-sm" />
                  <div className="flex flex-col">
                    <span className="text-[14px] font-bold text-[var(--app-text)]">{userHandle.replace('@', '')}</span>
                    <span className="text-[14px] text-[var(--app-muted)]">{user.displayName}</span>
                  </div>
                </div>
                {user.id !== currentUserId && (
                  <Button 
                    variant={isUserFollowing ? "secondary" : "primary"} 
                    className={`!rounded-xl !h-[34px] px-5 font-bold text-[14px] ${isUserFollowing ? '!border-[var(--app-border-strong)] text-[var(--app-text)]' : '!bg-[var(--app-active)] !text-[var(--app-surface)] hover:opacity-80'}`}
                    onClick={() => handleFollowClick(user, isUserFollowing)}
                  >
                    {isUserFollowing ? 'Đang theo dõi' : 'Theo dõi'}
                  </Button>
                )}
              </div>
            )
          })}
          {!(followModal === 'followers' ? modalUsers.followers : modalUsers.following).length ? <p className="text-[14px] text-zinc-500 text-center py-6">Danh sách trống.</p> : null}
        </div>
      </Modal>

      <UnfollowConfirmModal 
        open={Boolean(unfollowTarget)}
        user={unfollowTarget}
        onClose={() => setUnfollowTarget(null)}
        onConfirm={confirmUnfollow}
      />

      <PostComposer mode={composerOpen ? 'modal' : null} onClose={() => setComposerOpen(false)} />
    </>
  );
}
