import { useEffect, useState } from 'react';
import { adminApi } from '../../../api/index.js';
import {
  createCurrentWeekReportTrend as createCurrentWeekPostTrend,
  isBeforeCurrentWeek,
} from '../utils/adminReportStatistics.js';

const STATISTICS_PAGE_SIZE = 100;

export function useAdminPostStatistics() {
  const [statistics, setStatistics] = useState({ totalPosts: 0, hiddenPosts: 0, weeklyTrend: [] });
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    const controller = new AbortController();
    const now = new Date();

    async function loadStatistics() {
      try {
        const [totalResponse, hiddenResponse, weeklyPosts] = await Promise.all([
          adminApi.getPosts({ page: 0, size: 1 }, controller.signal),
          adminApi.getPosts({ status: 'HIDDEN', page: 0, size: 1 }, controller.signal),
          loadCurrentWeekPosts(controller.signal, now),
        ]);
        setStatistics({
          totalPosts: totalResponse.totalElements || 0,
          hiddenPosts: hiddenResponse.totalElements || 0,
          weeklyTrend: createCurrentWeekPostTrend(weeklyPosts, now),
        });
        setError('');
      } catch (requestError) {
        if (requestError.code !== 'ERR_CANCELED') {
          setError(requestError.message || 'Không thể tải thống kê bài viết.');
        }
      } finally {
        if (!controller.signal.aborted) setLoading(false);
      }
    }

    const timer = window.setTimeout(loadStatistics, 0);
    return () => {
      window.clearTimeout(timer);
      controller.abort();
    };
  }, []);

  return { ...statistics, loading, error };
}

async function loadCurrentWeekPosts(signal, now) {
  const posts = [];
  let page = 0;
  let shouldContinue = true;

  while (shouldContinue) {
    const response = await adminApi.getPosts({ page, size: STATISTICS_PAGE_SIZE }, signal);
    const content = response.content || [];
    posts.push(...content);

    // Danh sách giảm dần theo createdAt nên dừng ngay khi đã đi qua đầu tuần hiện tại.
    const reachedPreviousWeek = content.some((post) => isBeforeCurrentWeek(post.createdAt, now));
    shouldContinue = !reachedPreviousWeek && page + 1 < (response.totalPages || 0);
    page += 1;
  }

  return posts;
}
