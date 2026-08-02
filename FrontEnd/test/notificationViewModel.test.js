import test from 'node:test';
import assert from 'node:assert/strict';
import {
  getNotificationPresentation,
  getNotificationTarget,
  normalizeNotificationPage,
} from '../src/features/notification/utils/notificationViewModel.js';

test('tạo nội dung tương tác từ actor và điều hướng ưu tiên đến bài viết', () => {
  const notification = {
    type: 'POST_COMMENT',
    postId: 12,
    actor: { userId: 7, displayName: 'Nguyễn An', avatarUrl: '/avatar.jpg' },
  };

  assert.deepEqual(getNotificationPresentation(notification), {
    actorName: 'Nguyễn An',
    avatarUrl: '/avatar.jpg',
    message: 'Nguyễn An đã bình luận bài viết của bạn',
    isSystem: false,
  });
  assert.equal(getNotificationTarget(notification), '/posts/12');
});

test('thông báo hệ thống không gắn tên actor và không điều hướng khi thiếu tài nguyên', () => {
  const notification = { type: 'ACCOUNT_UNBLOCKED', actor: null };

  assert.equal(getNotificationPresentation(notification).message, 'Tài khoản của bạn đã được mở khóa');
  assert.equal(getNotificationTarget(notification), null);
});

test('hiển thị POST_REPOST và điều hướng về bài gốc', () => {
  const notification = {
    type: 'POST_REPOST',
    postId: 100,
    actor: { userId: 20, displayName: 'Minh', avatarUrl: null },
  };
  assert.equal(getNotificationPresentation(notification).message, 'Minh đã đăng lại bài viết của bạn');
  assert.equal(getNotificationTarget(notification), '/posts/100');
});

test('chuẩn hóa page an toàn khi backend không trả đủ trường tùy chọn', () => {
  assert.deepEqual(normalizeNotificationPage({ content: null, page: 2, last: false }), {
    content: [],
    page: 2,
    last: false,
  });
  assert.deepEqual(normalizeNotificationPage(undefined), { content: [], page: 0, last: true });
});
