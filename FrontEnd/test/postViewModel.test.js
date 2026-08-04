import test from 'node:test';
import assert from 'node:assert/strict';
import { toFeedItemView, toPostEditDraft } from '../src/features/post/utils/postViewModel.js';

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
