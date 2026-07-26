import ContentShell from '../../../components/layout/ContentShell.jsx';
import InfinitePostList from './InfinitePostList.jsx';
import { useInfinitePosts } from '../hooks/useInfinitePosts.js';
import { toPostView } from '../utils/postViewModel.js';

/**
 * Khung dùng chung cho các danh sách bài viết cá nhân có cùng hành vi Infinite Scroll.
 * Cấu hình chỉ thay đổi nội dung hiển thị và cách loại bài sau tương tác.
 */
export default function PersonalPostListPage({
  title,
  cacheKey,
  request,
  interaction,
  errorTitle,
  emptyTitle,
  emptyDescription,
}) {
  const state = useInfinitePosts({
    cacheKey,
    request,
    normalizePost: toPostView,
  });

  const interactionProps = interaction === 'save'
    ? { onSaveChange: (postId, saved) => { if (!saved) state.removePost(postId); } }
    : { onLikeChange: (postId, liked) => { if (!liked) state.removePost(postId); } };

  const header = (
    <div className="flex h-[var(--header-height)] items-center px-6">
      <h2 className="text-[17px] font-bold text-[var(--app-text)]">{title}</h2>
    </div>
  );

  return (
    <ContentShell header={header}>
      <div className="pb-20">
        <InfinitePostList
          {...state}
          {...interactionProps}
          errorTitle={errorTitle}
          emptyTitle={emptyTitle}
          emptyDescription={emptyDescription}
        />
      </div>
    </ContentShell>
  );
}
