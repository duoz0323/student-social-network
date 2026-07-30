import test from 'node:test';
import assert from 'node:assert/strict';
import { toPostEditDraft } from '../src/features/post/utils/postViewModel.js';

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

test('tạo draft an toàn khi Post Detail không có hashtag, media hoặc Location', () => {
  const draft = toPostEditDraft({ postId: 12, content: null });
  assert.equal(draft.post.id, 12);
  assert.equal(draft.content, '');
  assert.equal(draft.hashtag, '');
  assert.deepEqual(draft.keepMediaIds, []);
  assert.equal(draft.location, null);
});
