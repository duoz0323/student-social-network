import { postApi } from '../../../api/index.js';
import PersonalPostListPage from '../components/PersonalPostListPage.jsx';

export default function SavedPostsPage() {
  return (
    <PersonalPostListPage
      title="Đã lưu"
      cacheKey="posts:saved"
      request={postApi.getSaved}
      interaction="save"
      errorTitle="Không thể tải bài viết đã lưu"
      emptyTitle="Chưa lưu bài viết"
      emptyDescription="Các bài bạn lưu sẽ xuất hiện tại đây."
    />
  );
}
