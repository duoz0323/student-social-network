import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { socialApi } from '../../../api/index.js';
import Avatar from '../../../components/common/Avatar.jsx';
import Button from '../../../components/common/Button.jsx';
import { EmptyState, LoadingState } from '../../../components/common/StateBlock.jsx';
import ContentShell from '../../../components/layout/ContentShell.jsx';
import { useApp } from '../../../contexts/AppContext.jsx';
import PostCard from '../../post/components/PostCard.jsx';

function normalizePost(post) {
  return {
    ...post,
    id: post.postId,
    authorId: post.author?.id,
    imageUrls: (post.media ?? []).map((item) => item.url),
    hashtags: post.hashtag ? [post.hashtag] : [],
    edited: post.isEdited,
  };
}

export default function SearchPage() {
  const navigate = useNavigate();
  const { currentUserId } = useApp();
  const [query, setQuery] = useState('');
  const [users, setUsers] = useState([]);
  const [posts, setPosts] = useState([]);
  const [following, setFollowing] = useState(new Set());
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  useEffect(() => {
    const keyword = query.trim();
    if (!keyword) {
      const resetTimer = setTimeout(() => { setUsers([]); setPosts([]); setError(''); }, 0);
      return () => clearTimeout(resetTimer);
    }
    const controller = new AbortController();
    const timer = setTimeout(async () => {
      setLoading(true);
      try {
        const isHashtag = keyword.startsWith('#');
        const [userPage, postPage] = await Promise.all([
          socialApi.searchUsers({ q: keyword, page: 0, size: 20 }, controller.signal),
          socialApi.searchPosts({ q: keyword, type: isHashtag ? 'HASHTAG' : 'CONTENT', page: 0, size: 20 }, controller.signal),
        ]);
        setUsers((userPage.content ?? []).map((user) => ({ ...user, id: user.userId })));
        setPosts((postPage.content ?? []).map(normalizePost));
        setError('');
      } catch (requestError) {
        if (requestError.code !== 'ERR_CANCELED') setError(requestError.message);
      } finally {
        setLoading(false);
      }
    }, 350);
    return () => {
      clearTimeout(timer);
      controller.abort();
    };
  }, [query]);

  async function toggleFollow(userId) {
    try {
      const response = following.has(userId)
        ? await socialApi.unfollow(userId)
        : await socialApi.follow(userId);
      setFollowing((current) => {
        const next = new Set(current);
        if (response.followedByCurrentUser) next.add(userId); else next.delete(userId);
        return next;
      });
    } catch (requestError) {
      setError(requestError.message);
    }
  }

  const header = (
    <div className="px-6 py-3">
      <input value={query} onChange={(event) => setQuery(event.target.value)} placeholder="Tìm người dùng, nội dung hoặc #hashtag"
        className="h-11 w-full rounded-full bg-[var(--app-surface-soft)] px-5 outline-none focus:ring-1 focus:ring-[var(--app-border-strong)]" />
    </div>
  );

  return (
    <ContentShell header={header}>
      {!query.trim() ? (
        <EmptyState title="Tìm kiếm UniShare" description="Nhập tên người dùng, nội dung bài viết hoặc hashtag." />
      ) : loading ? <LoadingState message="Đang tìm kiếm..." /> : (
        <div className="pb-16">
          {error && <p className="m-4 rounded-xl bg-red-50 p-3 text-sm text-red-700">{error}</p>}
          {users.length > 0 && (
            <section className="border-b border-[var(--app-border)] p-5">
              <h2 className="mb-3 font-bold">Người dùng</h2>
              {users.map((user) => (
                <div key={user.id} className="flex items-center gap-3 py-2">
                  <button className="flex min-w-0 flex-1 items-center gap-3 text-left" onClick={() => navigate(String(user.id) === String(currentUserId) ? '/profile/me' : `/profile/${user.id}`)}>
                    <Avatar src={user.avatarUrl} name={user.displayName} />
                    <span><strong>{user.displayName}</strong><small className="block text-[var(--app-muted)]">{user.bio || 'Chưa có giới thiệu'}</small></span>
                  </button>
                  {String(user.id) !== String(currentUserId) && (
                    <Button size="sm" variant={following.has(user.id) ? 'secondary' : 'primary'} onClick={() => toggleFollow(user.id)}>
                      {following.has(user.id) ? 'Bỏ theo dõi' : 'Theo dõi'}
                    </Button>
                  )}
                </div>
              ))}
            </section>
          )}
          {posts.map((post) => <PostCard key={post.id} post={post} />)}
          {!error && users.length === 0 && posts.length === 0 && (
            <EmptyState title="Không có kết quả" description="Thử lại với một từ khóa khác." />
          )}
        </div>
      )}
    </ContentShell>
  );
}
