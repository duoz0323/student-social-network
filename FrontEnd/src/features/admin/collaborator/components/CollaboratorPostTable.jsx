import { Link } from 'react-router-dom';

export default function CollaboratorPostTable({ posts = [] }) {
  if (!posts.length) return <p className="rounded-2xl border border-dashed border-zinc-300 p-10 text-center text-zinc-500">Chưa có nội dung phù hợp.</p>;
  return <div className="overflow-hidden rounded-2xl border border-zinc-200 bg-white">
    {posts.map((post) => <article key={post.postId} className="grid gap-4 border-b border-zinc-100 p-5 last:border-b-0 md:grid-cols-[72px_1fr_auto]">
      {post.thumbnail ? <img src={post.thumbnail} alt="" className="h-16 w-16 rounded-xl object-cover" /> : <div className="h-16 w-16 rounded-xl bg-zinc-100" />}
      <div className="min-w-0">
        <p className="line-clamp-2 font-medium text-zinc-900">{post.contentPreview || 'Bài viết chỉ có media'}</p>
        <p className="mt-2 text-sm text-zinc-500">{post.hashtag ? `#${post.hashtag} · ` : ''}{post.likeCount} thích · {post.commentCount} bình luận · {post.repostCount} đăng lại</p>
        <span className="mt-2 inline-block rounded-full bg-zinc-100 px-2.5 py-1 text-xs font-semibold">{post.status}</span>
      </div>
      <div className="flex items-center text-sm font-semibold">
        <Link className="text-indigo-600 hover:underline" to={`/admin/collaborator/posts/${post.postId}`}>Chi tiết</Link>
      </div>
    </article>)}
  </div>;
}
