import assert from 'node:assert/strict';
import test from 'node:test';
import { shouldSubmitComposerOnEnter } from './commentComposer.js';

test('Enter gửi bình luận', () => {
  assert.equal(shouldSubmitComposerOnEnter({ key: 'Enter' }), true);
});

test('Shift+Enter xuống dòng thay vì gửi', () => {
  assert.equal(shouldSubmitComposerOnEnter({ key: 'Enter', shiftKey: true }), false);
});

test('Enter không gửi khi bộ gõ IME đang ghép ký tự', () => {
  assert.equal(shouldSubmitComposerOnEnter({ key: 'Enter', nativeEvent: { isComposing: true } }), false);
});
