import test from 'node:test';
import assert from 'node:assert/strict';
import { moveSearchSuggestionIndex } from './searchSuggestionNavigation.js';

test('ArrowDown chọn dòng đầu tiên và quay lại đầu danh sách', () => {
  assert.equal(moveSearchSuggestionIndex(-1, 4, 'down'), 0);
  assert.equal(moveSearchSuggestionIndex(3, 4, 'down'), 0);
});

test('ArrowUp chọn dòng cuối cùng và quay lại cuối danh sách', () => {
  assert.equal(moveSearchSuggestionIndex(-1, 4, 'up'), 3);
  assert.equal(moveSearchSuggestionIndex(0, 4, 'up'), 3);
});

test('không chọn dòng nào khi dropdown rỗng', () => {
  assert.equal(moveSearchSuggestionIndex(0, 0, 'down'), -1);
});
