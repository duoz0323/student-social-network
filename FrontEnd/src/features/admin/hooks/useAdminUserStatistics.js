import { useEffect, useState } from 'react';
import { adminApi } from '../../../api/index.js';
import {
  createCurrentWeekReportTrend as createCurrentWeekUserTrend,
  isBeforeCurrentWeek,
} from '../utils/adminReportStatistics.js';

const STATISTICS_PAGE_SIZE = 100;

export function useAdminUserStatistics(refreshKey = 0) {
  const [statistics, setStatistics] = useState({ activeUsers: 0, blockedUsers: 0, weeklyTrend: [] });
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    const controller = new AbortController();
    const now = new Date();

    async function loadStatistics() {
      setLoading(true);
      try {
        const [activeResponse, blockedResponse, weeklyUsers] = await Promise.all([
          adminApi.getUsers({ status: 'ACTIVE', page: 0, size: 1 }, controller.signal),
          adminApi.getUsers({ status: 'BLOCKED', page: 0, size: 1 }, controller.signal),
          loadCurrentWeekUsers(controller.signal, now),
        ]);
        setStatistics({
          activeUsers: activeResponse.totalElements || 0,
          blockedUsers: blockedResponse.totalElements || 0,
          weeklyTrend: createCurrentWeekUserTrend(weeklyUsers, now),
        });
        setError('');
      } catch (requestError) {
        if (requestError.code !== 'ERR_CANCELED') {
          setError(requestError.message || 'Không thể tải thống kê người dùng.');
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
  }, [refreshKey]);

  return { ...statistics, loading, error };
}

async function loadCurrentWeekUsers(signal, now) {
  const users = [];
  let page = 0;
  let shouldContinue = true;

  while (shouldContinue) {
    const response = await adminApi.getUsers({ page, size: STATISTICS_PAGE_SIZE }, signal);
    const content = response.content || [];
    users.push(...content);

    // Danh sách giảm dần theo thời gian tạo nên có thể dừng khi đi qua đầu tuần.
    const reachedPreviousWeek = content.some((user) => isBeforeCurrentWeek(user.createdAt, now));
    shouldContinue = !reachedPreviousWeek && page + 1 < (response.totalPages || 0);
    page += 1;
  }

  return users;
}
