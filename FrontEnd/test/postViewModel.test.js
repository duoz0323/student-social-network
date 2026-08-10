import test from 'node:test';
import assert from 'node:assert/strict';
import { copyPostLink, toFeedItemView, toPostEditDraft } from '../src/features/post/utils/postViewModel.js';

test('tạo draft sửa từ Post Detail và giữ đúng media cùng Location', () => {
  const location = { id: 7, placeId: 'ChIJ-detail', displayName: 'Cao Lỗ' };
  const draft = toPostEditDraft({
    id: 11,
    content: 'Nội dung mới nhất',
    hashtag: 'sinhvien',
    media: [{ id: 101, url: '/one.jpg' }, { id: 102, url: '/two.jpg' }],
    location,
  });

  assert.equal(draft.post.id, 11);
  assert.equal(draft.content, 'Nội dung mới nhất');
  assert.equal(draft.hashtag, 'sinhvien');
  assert.deepEqual(draft.keepMediaIds, [101, 102]);
  assert.deepEqual(draft.location, location);
});

test('chuẩn hóa Repost activity và giữ khóa item riêng khi cùng tham chiếu một bài gốc', () => {
  const item = toFeedItemView({
    itemType: 'REPOST',
    activityAt: '2026-08-01T10:00:00',
    repostedAt: '2026-08-01T10:00:00',
    repostedBy: { id: 20, displayName: 'Minh' },
    post: { postId: 100, repostCount: 3, repostedByCurrentUser: true },
  });

  assert.equal(item.id, 100);
  assert.equal(item.itemType, 'REPOST');
  assert.equal(item.repostedBy.id, 20);
  assert.equal(item.repostCount, 3);
  assert.equal(item.repostedByCurrentUser, true);
  assert.match(item.feedItemKey, /^REPOST:20:100:/);
});

test('tạo draft an toàn khi Post Detail không có hashtag, media hoặc Location', () => {
  const draft = toPostEditDraft({ postId: 12, content: null });
  assert.equal(draft.post.id, 12);
  assert.equal(draft.content, '');
  assert.equal(draft.hashtag, '');
  assert.deepEqual(draft.keepMediaIds, []);
  assert.equal(draft.location, null);
});

test('sao chép permalink đã encode và trả lại URL vừa ghi vào clipboard', async () => {
  const windowDescriptor = Object.getOwnPropertyDescriptor(globalThis, 'window');
  const navigatorDescriptor = Object.getOwnPropertyDescriptor(globalThis, 'navigator');
  let copiedText = '';

  Object.defineProperty(globalThis, 'window', {
    configurable: true,
    value: { location: { origin: 'https://unis.example' } },
  });
  Object.defineProperty(globalThis, 'navigator', {
    configurable: true,
    value: { clipboard: { writeText: async (value) => { copiedText = value; } } },
  });

  try {
    const copiedUrl = await copyPostLink('bài viết 1');
    assert.equal(copiedText, 'https://unis.example/posts/b%C3%A0i%20vi%E1%BA%BFt%201');
    assert.equal(copiedUrl, copiedText);
  } finally {
    if (windowDescriptor) Object.defineProperty(globalThis, 'window', windowDescriptor);
    else delete globalThis.window;
    if (navigatorDescriptor) Object.defineProperty(globalThis, 'navigator', navigatorDescriptor);
    else delete globalThis.navigator;
  }
});

test('báo lỗi khi trình duyệt không cung cấp Clipboard API', async () => {
  const windowDescriptor = Object.getOwnPropertyDescriptor(globalThis, 'window');
  const navigatorDescriptor = Object.getOwnPropertyDescriptor(globalThis, 'navigator');

  Object.defineProperty(globalThis, 'window', {
    configurable: true,
    value: { location: { origin: 'https://unis.example' } },
  });
  Object.defineProperty(globalThis, 'navigator', { configurable: true, value: {} });

  try {
    await assert.rejects(copyPostLink(10), /không hỗ trợ sao chép tự động/);
  } finally {
    if (windowDescriptor) Object.defineProperty(globalThis, 'window', windowDescriptor);
    else delete globalThis.window;
    if (navigatorDescriptor) Object.defineProperty(globalThis, 'navigator', navigatorDescriptor);
    else delete globalThis.navigator;
  }
});
