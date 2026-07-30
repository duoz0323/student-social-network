import test from 'node:test';
import assert from 'node:assert/strict';
import { toAdminReportPostView, toReportPostFallback } from '../src/features/admin/utils/adminReportPost.js';

test('chuẩn hóa Admin Post Detail để hiển thị đầy đủ nội dung và media', () => {
  const result = toAdminReportPostView({
    postId: 42,
    content: 'Nội dung bài viết',
    hashtag: 'cau lac bo',
    createdAt: '2026-07-30T03:40:00',
    author: { userId: 7, displayName: 'Đậu Quốc Khánh', avatarUrl: '/avatar.jpg' },
    media: [
      { mediaId: 2, mediaUrl: '/second.jpg', mediaType: 'IMAGE', sortOrder: 1 },
      { mediaId: 1, mediaUrl: '/first.jpg', mediaType: 'IMAGE', sortOrder: 0 },
    ],
  });

  assert.equal(result.id, 42);
  assert.equal(result.author.id, 7);
  assert.deepEqual(result.hashtags, ['cau lac bo']);
  assert.deepEqual(result.media.map((item) => item.url), ['/second.jpg', '/first.jpg']);
  assert.deepEqual(result.media.map((item) => item.displayOrder), [1, 0]);
});

test('dùng snapshot báo cáo làm nội dung dự phòng khi API bài viết lỗi', () => {
  const result = toReportPostFallback({
    createdAt: '2026-07-30T03:40:00',
    reportedPost: {
      postId: 42,
      currentStatus: 'DELETED',
      currentContent: null,
      author: { userId: 7, displayName: 'Đậu Quốc Khánh', avatarUrl: null },
    },
    evidence: {
      contentSnapshot: 'Nội dung tại thời điểm báo cáo',
      mediaSnapshot: ['/snapshot.jpg'],
    },
  });

  assert.equal(result.content, 'Nội dung tại thời điểm báo cáo');
  assert.equal(result.media[0].url, '/snapshot.jpg');
  assert.equal(result.status, 'DELETED');
});
