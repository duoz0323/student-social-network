import { useMemo, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import Avatar from '../../../components/common/Avatar.jsx';
import Button from '../../../components/common/Button.jsx';
import { EmptyState } from '../../../components/common/StateBlock.jsx';
import { useApp } from '../../../contexts/AppContext.jsx';
import PostCard from '../../post/components/PostCard.jsx';
import ContentShell from '../../../components/layout/ContentShell.jsx';

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

  const headerContent = (
    <div className="px-6 pb-3 pt-2">
      <div className="relative flex items-center">
        <div className="pointer-events-none absolute inset-y-0 left-0 flex items-center pl-4 text-zinc-400">
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round">
            <circle cx="11" cy="11" r="8"/>
            <path d="m21 21-4.3-4.3"/>
          </svg>
        </div>
        <input
          value={query}
          onChange={(event) => setQuery(event.target.value)}
          placeholder="Tìm kiếm"
          className="h-[44px] w-full rounded-full bg-zinc-100/80 pl-11 pr-4 text-[15px] text-[var(--app-text)] placeholder:text-zinc-400 outline-none border border-transparent focus:border-zinc-300 focus:bg-white transition"
        />
      </div>
    </div>
  );

  return (
    <ContentShell header={headerContent}>
      <div className="pb-20">
        {!normalized ? (
          <div className="flex flex-col">
            <div className="border-b border-[var(--app-border)] px-6 py-5">
              <h2 className="text-[15px] font-bold text-[var(--app-text)] mb-3">Tìm kiếm phổ biến</h2>
              <div className="flex flex-col">
                {[
                  { title: 'Bí quyết ôn thi cuối kỳ', sub: '1.2k bài viết' }, 
                  { title: 'Quán ăn quanh trường', sub: 'Chia sẻ thực tế' }, 
                  { title: 'Việc làm thêm cho sinh viên', sub: 'Hơn 500 cơ hội' }, 
                  { title: 'Tài liệu giải tích 2', sub: 'PDF & Notes' }
                ].map((item, idx) => (
                  <div key={idx} className="flex items-center gap-4 py-3 border-b border-[var(--app-border)] last:border-0 cursor-pointer hover:bg-zinc-50" onClick={() => setQuery(item.title)}>
                    <div className="flex h-[42px] w-[42px] shrink-0 items-center justify-center rounded-full border border-[var(--app-border)] text-[var(--app-text)]">
                      <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><polyline points="22 7 13.5 15.5 8.5 10.5 2 17"/><polyline points="16 7 22 7 22 13"/></svg>
                    </div>
                    <div className="flex flex-col">
                      <span className="text-[15px] font-semibold text-[var(--app-text)]">{item.title}</span>
                      <span className="text-[13px] text-[var(--app-muted)] mt-0.5">{item.sub}</span>
                    </div>
                  </div>
                ))}
              </div>
            </div>

            <div className="px-6 py-5">
              <h2 className="text-[15px] font-bold text-[var(--app-text)] mb-3">Gợi ý theo dõi</h2>
              <div className="flex flex-col">
                {suggestions.map((user, idx) => {
                  const isFollowing = data.follows.some((follow) => follow.followerId === currentUserId && follow.followingId === user.id);
                  const userHandle = user.email ? `@${user.email.split('@')[0]}` : `@user${user.id.slice(-4)}`;
                  const followersCount = (user.id.charCodeAt(0) * 10) + 120; // Fake followers count for mockup visually
                  
                  return (
                    <div key={user.id} className={`flex items-start justify-between py-3 ${idx !== suggestions.length - 1 ? 'border-b border-[var(--app-border)]' : ''}`}>
                      <div className="flex items-start gap-3 flex-1 cursor-pointer" onClick={() => navigate(`/profile/${user.id}`)}>
                        <Avatar src={user.avatarUrl} name={user.displayName} size="md" className="!w-10 !h-10 text-sm mt-0.5" />
                        <div className="flex flex-col pr-4">
                          <span className="text-[15px] font-semibold text-[var(--app-text)] flex items-center gap-1">
                            {user.displayName}
                            {idx === 0 && <svg className="w-3.5 h-3.5 text-blue-500" viewBox="0 0 24 24" fill="currentColor"><path d="M12 2C6.5 2 2 6.5 2 12s4.5 10 10 10 10-4.5 10-10S17.5 2 12 2zm-1.9 14.7L6 12.6l1.5-1.5 2.6 2.6 6.4-6.4 1.5 1.5-7.9 7.9z"/></svg>}
                          </span>
                          <span className="text-[14px] text-[var(--app-muted)]">{userHandle}</span>
                          <span className="text-[14px] text-[var(--app-text)] mt-1.5 leading-snug line-clamp-2">{user.bio || 'Sinh viên chăm chỉ.'}</span>
                          <span className="text-[14px] text-[var(--app-muted)] mt-1.5">{followersCount} người theo dõi</span>
                        </div>
                      </div>
                      <Button 
                        variant={isFollowing ? "secondary" : "primary"} 
                        className={`shrink-0 !rounded-xl !h-[34px] px-5 font-bold text-[14px] mt-1 ${isFollowing ? '!border-zinc-300 text-[var(--app-text)]' : '!bg-black !text-white hover:!bg-zinc-800'}`}
                        onClick={() => toggleFollow(user.id)}
                      >
                        {isFollowing ? 'Đang theo dõi' : 'Theo dõi'}
                      </Button>
                    </div>
                  );
                })}
              </div>
            </div>
          </div>
        ) : (
          <div className="flex flex-col pt-4">
            {results.users.length > 0 && (
              <div className="px-6 pb-4 border-b border-[var(--app-border)]">
                <h2 className="text-[15px] font-bold text-[var(--app-text)] mb-3">Người dùng</h2>
                <div className="flex flex-col">
                  {results.users.map((user) => {
                    const userHandle = user.email ? `@${user.email.split('@')[0]}` : `@user${user.id.slice(-4)}`;
                    return (
                      <div key={user.id} className="flex items-center gap-3 py-2 cursor-pointer hover:bg-zinc-50 rounded-lg px-2 -mx-2" onClick={() => navigate(user.id === currentUserId ? '/profile/me' : `/profile/${user.id}`)}>
                        <Avatar src={user.avatarUrl} name={user.displayName} size="md" className="!w-10 !h-10 text-sm" />
                        <div className="flex flex-col">
                          <span className="text-[15px] font-semibold text-[var(--app-text)]">{user.displayName}</span>
                          <span className="text-[14px] text-[var(--app-muted)]">{userHandle}</span>
                        </div>
                      </div>
                    );
                  })}
                </div>
              </div>
            )}
            
            {results.hashtags.length > 0 && (
              <div className="px-6 py-4 border-b border-[var(--app-border)] flex flex-wrap gap-2">
                {results.hashtags.map((tag) => (
                  <span key={tag} className="rounded-xl border border-zinc-200 bg-zinc-50 px-4 py-2 text-[14px] font-semibold text-black cursor-pointer hover:bg-zinc-100 transition" onClick={() => setQuery(`#${tag}`)}>
                    #{tag}
                  </span>
                ))}
              </div>
            )}
            
            <div className="flex flex-col">
              {results.posts.length ? (
                results.posts.map((post) => <PostCard key={post.id} post={post} />)
              ) : (
                <div className="px-5 py-8">
                  <EmptyState title="Không có kết quả" description="Thử lại bằng một từ khóa khác nhé." />
                </div>
              )}
            </div>
          </div>
        )}
      </div>
    </ContentShell>
  );
}
