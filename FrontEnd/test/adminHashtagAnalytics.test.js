import assert from 'node:assert/strict';
import test from 'node:test';
import { normalizeHashtagAnalytics, summarizeHashtagAnalytics } from '../src/features/admin/utils/adminHashtagAnalytics.js';

test('tổng hợp số hashtag đã dùng, chưa dùng và tổng lượt gắn bài viết', () => {
  const result = summarizeHashtagAnalytics([
    { hashtagId: 1, name: 'hoc-tap', postCount: 8 },
    { hashtagId: 2, name: 'su-kien', postCount: 0 },
    { hashtagId: 3, name: 'do-an', postCount: 3 },
  ], 3);

  assert.equal(result.totalHashtagCount, 3);
  assert.equal(result.usedHashtagCount, 2);
  assert.equal(result.unusedHashtagCount, 1);
  assert.equal(result.totalPostUsages, 11);
  assert.deepEqual(result.topHashtags.map((item) => item.name), ['hoc-tap', 'do-an', 'su-kien']);
});

test('chuẩn hóa postCount không hợp lệ thành số không âm', () => {
  const result = summarizeHashtagAnalytics([
    { name: 'a', postCount: -2 },
    { name: 'b', postCount: '4' },
    { name: 'c', postCount: null },
  ], 3);

  assert.equal(result.totalPostUsages, 4);
  assert.equal(result.usedHashtagCount, 1);
});

test('chuẩn hóa đầy đủ contract hashtag analytics và giữ change rate null', () => {
  const result = normalizeHashtagAnalytics({
    granularity: 'MONTH',
    kpis: { totalHashtags: '12', usedHashtags: 4, newHashtagsChangeRate: null, usageRate: '40.5' },
    trend: [{ period: '2026-08', postsWithHashtag: '8', totalPosts: 20 }],
    popularHashtags: [{ hashtagId: 1, name: 'hoctap', postCount: '3', share: '37.5' }],
    lowUsageHashtags: [{ hashtagId: 2, name: 'java21', linkedPostCount: -1 }],
  });

  assert.equal(result.granularity, 'MONTH');
  assert.equal(result.kpis.totalHashtags, 12);
  assert.equal(result.kpis.newHashtagsChangeRate, null);
  assert.equal(result.kpis.usageRate, 40.5);
  assert.deepEqual(result.trend[0], { period: '2026-08', postsWithHashtag: 8, totalPosts: 20 });
  assert.equal(result.popularHashtags[0].share, 37.5);
  assert.equal(result.lowUsageHashtags[0].linkedPostCount, 0);
});
