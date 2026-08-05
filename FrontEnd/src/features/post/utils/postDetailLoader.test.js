import assert from 'node:assert/strict';
import test from 'node:test';
import { loadPostDetailData } from './postDetailLoader.js';

test('vẫn trả Post Detail khi request bình luận thất bại', async () => {
  const commentError = new Error('Không thể tải bình luận');
  const result = await loadPostDetailData(
    Promise.resolve({ id: 15, content: 'Bài viết' }),
    Promise.reject(commentError),
  );

  assert.equal(result.post.id, 15);
  assert.deepEqual(result.comments, []);
  assert.equal(result.commentsError, commentError);
});

test('không dựng trang chi tiết khi chính request bài viết thất bại', async () => {
  const postError = new Error('Không tìm thấy bài viết');

  await assert.rejects(
    loadPostDetailData(Promise.reject(postError), Promise.resolve({ content: [] })),
    postError,
  );
});

test('chuẩn hóa danh sách bình luận hợp lệ', async () => {
  const comments = [{ commentId: 1, content: 'Bình luận' }];
  const result = await loadPostDetailData(
    Promise.resolve({ id: 15 }),
    Promise.resolve({ content: comments }),
  );

  assert.deepEqual(result.comments, comments);
  assert.equal(result.commentsError, null);
});
