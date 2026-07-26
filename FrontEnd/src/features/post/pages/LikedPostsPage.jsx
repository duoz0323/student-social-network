import { postApi } from '../../../api/index.js';
import PersonalPostListPage from '../components/PersonalPostListPage.jsx';

export default function LikedPostsPage() {
  return (
    <PersonalPostListPage
      title="Đã thích"
      cacheKey="posts:liked"
      request={postApi.getLiked}
      interaction="like"
      errorTitle="Không thể tải bài viết đã thích"
      emptyTitle="Chưa thích bài viết"
      emptyDescription="Các bài bạn thích sẽ xuất hiện tại đây."
    />
  );
}
