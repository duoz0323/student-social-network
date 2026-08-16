/** Tổng hợp dữ liệu hashtag đã phân trang thành các chỉ số dùng trên màn hình Analytics. */
export function summarizeHashtagAnalytics(items = [], totalElements = items.length) {
  const normalizedItems = items.map((item) => ({
    ...item,
    postCount: Math.max(0, Number(item?.postCount) || 0),
  }));
  const usedHashtagCount = normalizedItems.filter((item) => item.postCount > 0).length;

  return {
    totalHashtagCount: Math.max(0, Number(totalElements) || 0),
    usedHashtagCount,
    unusedHashtagCount: Math.max(0, normalizedItems.length - usedHashtagCount),
    totalPostUsages: normalizedItems.reduce((total, item) => total + item.postCount, 0),
    topHashtags: [...normalizedItems]
      .sort((left, right) => right.postCount - left.postCount || String(left.name).localeCompare(String(right.name), 'vi'))
      .slice(0, 10),
  };
}

/** Chuẩn hóa response analytics để mọi card và chart có giá trị hiển thị an toàn. */
export function normalizeHashtagAnalytics(payload = {}) {
  const kpis = payload.kpis || {};
  return {
    fromDate: payload.fromDate || '',
    toDate: payload.toDate || '',
    granularity: payload.granularity === 'MONTH' ? 'MONTH' : 'DAY',
    kpis: {
      totalHashtags: count(kpis.totalHashtags),
      usedHashtags: count(kpis.usedHashtags),
      newHashtags: count(kpis.newHashtags),
      newHashtagsChangeRate: nullableNumber(kpis.newHashtagsChangeRate),
      postsWithHashtag: count(kpis.postsWithHashtag),
      totalPosts: count(kpis.totalPosts),
      usageRate: number(kpis.usageRate),
      averagePostsPerUsedHashtag: number(kpis.averagePostsPerUsedHashtag),
    },
    trend: list(payload.trend).map((item) => ({
      period: item?.period || '', postsWithHashtag: count(item?.postsWithHashtag), totalPosts: count(item?.totalPosts),
    })),
    popularHashtags: list(payload.popularHashtags).map((item) => normalizeHashtag(item, 'postCount')),
    distribution: {
      topTenPosts: count(payload.distribution?.topTenPosts),
      otherPosts: count(payload.distribution?.otherPosts),
    },
    growthHashtags: list(payload.growthHashtags).map((item) => ({
      hashtagId: item?.hashtagId, name: item?.name || '', previousCount: count(item?.previousCount),
      currentCount: count(item?.currentCount), changeRate: nullableNumber(item?.changeRate),
    })),
    recentHashtags: list(payload.recentHashtags).map((item) => ({
      hashtagId: item?.hashtagId, name: item?.name || '', linkedPostCount: count(item?.linkedPostCount),
      periodPostCount: count(item?.periodPostCount), createdAt: item?.createdAt || null, latestUsedAt: item?.latestUsedAt || null,
    })),
    lowUsageHashtags: list(payload.lowUsageHashtags).map((item) => normalizeHashtag(item, 'linkedPostCount')),
  };
}

function normalizeHashtag(item, countKey) {
  return {
    hashtagId: item?.hashtagId,
    name: item?.name || '',
    [countKey]: count(item?.[countKey]),
    ...(countKey === 'postCount' ? { share: number(item?.share) } : {}),
  };
}

function list(value) { return Array.isArray(value) ? value : []; }
function number(value) { const parsed = Number(value); return Number.isFinite(parsed) ? parsed : 0; }
function nullableNumber(value) { return value === null || value === undefined || value === '' ? null : number(value); }
function count(value) { return Math.max(0, number(value)); }
