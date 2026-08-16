import assert from 'node:assert/strict';
import test from 'node:test';
import { nextSortDirection, sortTableRows } from '../src/components/common/tableSorting.js';

test('text và number chuyển tăng, giảm rồi trở về mặc định', () => {
  assert.equal(nextSortDirection(null, 'text'), 'asc');
  assert.equal(nextSortDirection('asc', 'number'), 'desc');
  assert.equal(nextSortDirection('desc', 'number'), null);
});

test('date chuyển mới đến cũ, cũ đến mới rồi trở về mặc định', () => {
  assert.equal(nextSortDirection(null, 'date'), 'desc');
  assert.equal(nextSortDirection('desc', 'date'), 'asc');
  assert.equal(nextSortDirection('asc', 'date'), null);
});

test('sắp xếp tiếng Việt, số và ngày đồng thời giữ giá trị trống ở cuối', () => {
  const rows = [
    { name: 'Đức', count: 10, createdAt: '2026-01-02' },
    { name: 'An', count: 2, createdAt: null },
    { name: 'Bình', count: 1, createdAt: '2026-03-01' },
  ];
  assert.deepEqual(sortTableRows(rows, { key: 'name', sortType: 'text' }, 'asc').map((row) => row.name), ['An', 'Bình', 'Đức']);
  assert.deepEqual(sortTableRows(rows, { key: 'count', sortType: 'number' }, 'asc').map((row) => row.count), [1, 2, 10]);
  assert.deepEqual(sortTableRows(rows, { key: 'createdAt', sortType: 'date' }, 'desc').map((row) => row.name), ['Bình', 'Đức', 'An']);
});
