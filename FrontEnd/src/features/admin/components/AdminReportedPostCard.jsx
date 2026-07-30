import { MapPin, MoreVertical } from 'lucide-react';
import Avatar from '../../../components/common/Avatar.jsx';
import PostMediaGrid from '../../post/components/PostMediaGrid.jsx';
import { shortTime } from '../../../utils/formatters.js';
import { googleMapsLocationUrl } from '../../post/locations/locationUtils.js';

export default function AdminReportedPostCard({ post }) {
  if (!post?.author) return null;

  return (
    <article className="overflow-hidden rounded-xl bg-zinc-950 p-4 text-zinc-100 shadow-sm sm:p-5">
      <header className="flex items-center gap-3">
        <Avatar src={post.author.avatarUrl} name={post.author.displayName} size="sm" />
        <div className="min-w-0 flex-1">
          <p className="truncate text-sm font-bold text-white">
            {post.author.displayName}
            <span className="ml-2 font-normal text-zinc-400">· {shortTime(post.publishedAt)}</span>
          </p>
        </div>
        <MoreVertical aria-hidden="true" className="text-zinc-500" size={18} />
      </header>

      <div className="mt-3 pl-0 sm:pl-12">
        {post.content ? <p className="whitespace-pre-wrap text-[15px] leading-6 text-zinc-100">{post.content}</p> : null}
        {post.hashtags.length > 0 ? (
          <div className="mt-1 flex flex-wrap gap-2">
            {post.hashtags.map((hashtag) => (
              <span key={hashtag} className="text-sm font-semibold text-violet-400">#{hashtag}</span>
            ))}
          </div>
        ) : null}
        {post.location ? (
          <a
            href={googleMapsLocationUrl(post.location)}
            target="_blank"
            rel="noopener noreferrer"
            className="mt-2 flex items-center gap-1 text-sm text-zinc-400 transition hover:text-violet-300"
          >
            <MapPin size={14} />
            <span className="truncate">{post.location.displayName}</span>
          </a>
        ) : null}
        <PostMediaGrid post={post} />
      </div>
    </article>
  );
}
