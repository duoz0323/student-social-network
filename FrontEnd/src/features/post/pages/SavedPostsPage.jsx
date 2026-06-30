import { EmptyState } from '../../../components/common/StateBlock.jsx';
import { useApp } from '../../../contexts/AppContext.jsx';
import PostCard from '../components/PostCard.jsx';

export default function SavedPostsPage() {
  const { data, currentUserId, getPostById } = useApp();
  const posts = data.savedPosts
    .filter((item) => item.userId === currentUserId)
    .map((item) => getPostById(item.postId))
    .filter(Boolean);

  return (
    <section className="space-y-4 pb-20">
      <h1 className="text-2xl font-black">Bai viet da luu</h1>
      {posts.length ? posts.map((post) => <PostCard key={post.id} post={post} />) : <EmptyState title="Chua luu bai viet" description="Cac bai ban luu se xuat hien tai day." />}
    </section>
  );
}
