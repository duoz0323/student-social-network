import { useMemo, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import Avatar from '../../../components/common/Avatar.jsx';
import Button from '../../../components/common/Button.jsx';
import { EmptyState } from '../../../components/common/StateBlock.jsx';
import { useApp } from '../../../contexts/AppContext.jsx';
import PostCard from '../../post/components/PostCard.jsx';

export default function SearchPage() {
  const { data, publicPosts, currentUserId, toggleFollow } = useApp();
  const [query, setQuery] = useState('');
  const navigate = useNavigate();
  const normalized = query.trim().toLowerCase();

  const results = useMemo(() => {
    if (!normalized) return { users: [], posts: [], hashtags: [] };
    const users = data.users.filter((user) => user.status === 'ACTIVE' && user.displayName.toLowerCase().includes(normalized));
    const posts = publicPosts.filter((post) => post.content.toLowerCase().includes(normalized) || post.hashtags.some((tag) => tag.includes(normalized.replace('#', ''))));
    const hashtags = [...new Set(publicPosts.flatMap((post) => post.hashtags))].filter((tag) => tag.includes(normalized.replace('#', '')));
    return { users, posts, hashtags };
  }, [data.users, normalized, publicPosts]);

  const suggestions = data.users.filter((user) => user.status === 'ACTIVE' && user.id !== currentUserId).slice(0, 3);

  return (
    <section className="space-y-5 pb-20">
      <h1 className="text-2xl font-black">Tim kiem</h1>
      <input
        value={query}
        onChange={(event) => setQuery(event.target.value)}
        placeholder="Tim theo ten hien thi, noi dung bai viet hoac hashtag"
        className="w-full rounded-2xl border border-zinc-200 bg-white px-5 py-4 outline-none focus:border-zinc-950"
      />
      {!normalized ? (
        <div className="rounded-2xl border border-zinc-200 bg-white p-4">
          <h2 className="font-black">Goi y theo doi</h2>
          <div className="mt-4 space-y-3">
            {suggestions.map((user) => {
              const isFollowing = data.follows.some((follow) => follow.followerId === currentUserId && follow.followingId === user.id);
              return (
                <div key={user.id} className="flex items-center justify-between gap-3">
                  <button className="flex items-center gap-3 text-left" onClick={() => navigate(`/profile/${user.id}`)}>
                    <Avatar src={user.avatarUrl} name={user.displayName} size="sm" />
                    <div>
                      <p className="font-bold">{user.displayName}</p>
                      <p className="text-xs text-zinc-500">{user.bio}</p>
                    </div>
                  </button>
                  <Button size="sm" variant={isFollowing ? 'secondary' : 'primary'} onClick={() => toggleFollow(user.id)}>{isFollowing ? 'Bo theo doi' : 'Theo doi'}</Button>
                </div>
              );
            })}
          </div>
        </div>
      ) : (
        <>
          <div className="rounded-2xl border border-zinc-200 bg-white p-4">
            <h2 className="font-black">Nguoi dung</h2>
            <div className="mt-3 space-y-2">
              {results.users.map((user) => (
                <button key={user.id} className="flex w-full items-center gap-3 rounded-2xl p-2 text-left hover:bg-zinc-50" onClick={() => navigate(user.id === currentUserId ? '/profile/me' : `/profile/${user.id}`)}>
                  <Avatar src={user.avatarUrl} name={user.displayName} size="sm" />
                  <span className="font-bold">{user.displayName}</span>
                </button>
              ))}
              {!results.users.length ? <p className="text-sm text-zinc-500">Khong co nguoi dung phu hop.</p> : null}
            </div>
          </div>
          {results.hashtags.length ? <div className="flex flex-wrap gap-2">{results.hashtags.map((tag) => <span key={tag} className="rounded-full bg-violet-50 px-3 py-1 text-sm font-bold text-violet-700">#{tag}</span>)}</div> : null}
          {results.posts.length ? results.posts.map((post) => <PostCard key={post.id} post={post} />) : <EmptyState title="Khong co bai viet" description="Thu tu khoa khac hoac hashtag khac." />}
        </>
      )}
    </section>
  );
}
