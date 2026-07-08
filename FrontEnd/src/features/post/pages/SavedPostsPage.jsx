import { EmptyState } from '../../../components/common/StateBlock.jsx';
import { useApp } from '../../../contexts/AppContext.jsx';
import PostCard from '../components/PostCard.jsx';
import ContentShell from '../../../components/layout/ContentShell.jsx';

export default function SavedPostsPage() {
  const { data, currentUserId, getPostById } = useApp();
  const posts = data.savedPosts
    .filter((item) => item.userId === currentUserId)
    .map((item) => getPostById(item.postId))
    .filter(Boolean);

  const headerContent = (
    <div className="flex h-[var(--header-height)] items-center justify-between px-6">
      <h2 className="text-[17px] font-bold text-[var(--app-text)]">Đã lưu</h2>
    </div>
  );

  return (
    <ContentShell header={headerContent}>
      {/* Danh sách bài viết đã lưu */}
      <div className="pb-20">
        {posts.length ? (
          posts.map((post) => <PostCard key={post.id} post={post} />)
        ) : (
          <div className="px-5 py-8">
            <EmptyState title="Chưa lưu bài viết" description="Các bài bạn lưu sẽ xuất hiện tại đây." />
          </div>
        )}
      </div>
    </ContentShell>
  );
}
