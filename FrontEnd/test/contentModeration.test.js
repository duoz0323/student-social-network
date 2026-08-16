import test from 'node:test';
import assert from 'node:assert/strict';
import { readFile } from 'node:fs/promises';
import {
  getContentModerationMessage,
  isContentModerationError,
} from '../src/features/post/utils/contentModeration.js';

test('maps WARNING, BLOCK and unavailable to user-friendly wording', () => {
  assert.match(getContentModerationMessage({ code: 'CONTENT_MODERATION_WARNING' }), /chỉnh sửa trước khi đăng/);
  assert.match(getContentModerationMessage({ code: 'CONTENT_POLICY_VIOLATION' }), /Tiêu chuẩn cộng đồng/);
  assert.match(getContentModerationMessage({ code: 'CONTENT_MODERATION_UNAVAILABLE' }), /thử lại sau/);
});

test('does not classify unrelated API errors as moderation errors', () => {
  assert.equal(getContentModerationMessage({ code: 'POST_NOT_FOUND' }), null);
  assert.equal(isContentModerationError({ code: 'POST_NOT_FOUND' }), false);
  assert.equal(isContentModerationError({ code: 'CONTENT_MODERATION_WARNING' }), true);
});

test('Post, Comment và Reply chỉ clear draft sau response thành công', async () => {
  const [composer, detail] = await Promise.all([
    readFile(new URL('../src/features/post/components/PostComposer.jsx', import.meta.url), 'utf8'),
    readFile(new URL('../src/features/post/pages/PostDetailPage.jsx', import.meta.url), 'utf8'),
  ]);
  assert.match(composer, /if \(!result\.ok\)[\s\S]*?return;[\s\S]*?resetForm\(\)/);
  assert.match(detail, /await postApi\.createComment[\s\S]*?setComment\(''\)/);
  assert.match(detail, /await postApi\.createReply[\s\S]*?setReplyDrafts/);
  assert.doesNotMatch(detail, /setComments[\s\S]{0,120}await postApi\.createComment/);
});
