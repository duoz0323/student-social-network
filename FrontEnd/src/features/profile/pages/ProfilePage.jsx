import { useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import Avatar from '../../../components/common/Avatar.jsx';
import Button from '../../../components/common/Button.jsx';
import Modal from '../../../components/common/Modal.jsx';
import { EmptyState } from '../../../components/common/StateBlock.jsx';
import { useApp } from '../../../contexts/AppContext.jsx';
import PostCard from '../../post/components/PostCard.jsx';

export default function ProfilePage({ self = false }) {
  const { userId } = useParams();
  const navigate = useNavigate();
  const { currentUser, currentUserId, getUserById, data, toggleFollow, updateProfile } = useApp();
  const [editing, setEditing] = useState(false);
  const [followModal, setFollowModal] = useState(null);
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

  function openEdit() {
    setDraft({ displayName: profile.displayName, bio: profile.bio, avatarUrl: profile.avatarUrl });
    setEditing(true);
  }

  function saveProfile() {
    updateProfile(draft);
    setEditing(false);
  }

  return (
    <section className="space-y-4 pb-20">
      <div className="rounded-2xl border border-zinc-200 bg-white p-5">
        <div className="flex flex-col gap-5 sm:flex-row sm:items-end sm:justify-between">
          <div className="flex items-center gap-4">
            <Avatar src={profile.avatarUrl} name={profile.displayName} size="lg" />
            <div>
              <h1 className="text-2xl font-black">{profile.displayName}</h1>
              <p className="mt-2 max-w-xl text-sm text-zinc-600">{profile.bio || 'Chua co gioi thieu.'}</p>
            </div>
          </div>
          {isSelf ? (
            <Button variant="secondary" onClick={openEdit}>Chinh sua ho so</Button>
          ) : (
            <Button onClick={() => toggleFollow(profile.id)}>{isFollowing ? 'Bo theo doi' : 'Theo doi'}</Button>
          )}
        </div>
        <div className="mt-6 flex gap-6 text-sm">
          <button onClick={() => setFollowModal('followers')}><b>{profile.followerCount}</b> nguoi theo doi</button>
          <button onClick={() => setFollowModal('following')}><b>{profile.followingCount}</b> dang theo doi</button>
          <span><b>{posts.length}</b> bai viet</span>
        </div>
      </div>

      {posts.length ? posts.map((post) => <PostCard key={post.id} post={post} />) : <EmptyState title="Chua co bai viet" description="Cac bai da dang se hien thi tai day." />}

      <Modal
        open={editing}
        title="Chinh sua ho so"
        onClose={() => setEditing(false)}
        footer={
          <>
            <Button variant="secondary" onClick={() => setEditing(false)}>Huy</Button>
            <Button disabled={!draft.displayName.trim()} onClick={saveProfile}>Luu thay doi</Button>
          </>
        }
      >
        <label className="block text-sm font-semibold">Ten hien thi</label>
        <input className="mt-2 w-full rounded-2xl border border-zinc-200 p-3" value={draft.displayName} onChange={(event) => setDraft({ ...draft, displayName: event.target.value })} />
        <label className="mt-4 block text-sm font-semibold">Avatar URL</label>
        <input className="mt-2 w-full rounded-2xl border border-zinc-200 p-3" value={draft.avatarUrl} onChange={(event) => setDraft({ ...draft, avatarUrl: event.target.value })} />
        <label className="mt-4 block text-sm font-semibold">Bio</label>
        <textarea className="mt-2 min-h-24 w-full rounded-2xl border border-zinc-200 p-3" value={draft.bio} onChange={(event) => setDraft({ ...draft, bio: event.target.value })} />
      </Modal>

      <Modal open={Boolean(followModal)} title={followModal === 'followers' ? 'Nguoi theo doi' : 'Dang theo doi'} onClose={() => setFollowModal(null)}>
        <div className="space-y-3">
          {(followModal === 'followers' ? followers : following).map((user) => (
            <button key={user.id} className="flex w-full items-center gap-3 rounded-2xl p-2 text-left hover:bg-zinc-50" onClick={() => navigate(user.id === currentUserId ? '/profile/me' : `/profile/${user.id}`)}>
              <Avatar src={user.avatarUrl} name={user.displayName} size="sm" />
              <div>
                <p className="font-bold">{user.displayName}</p>
                <p className="text-xs text-zinc-500">{user.bio || 'Chua co bio'}</p>
              </div>
            </button>
          ))}
          {!(followModal === 'followers' ? followers : following).length ? <p className="text-sm text-zinc-500">Danh sach dang trong.</p> : null}
        </div>
      </Modal>
    </section>
  );
}
