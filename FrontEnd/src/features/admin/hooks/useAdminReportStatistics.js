import { useEffect, useState } from 'react';
import { adminApi } from '../../../api/index.js';
import { ADMIN_REPORT_STATUSES } from '../constants/adminReportLabels.js';
import { createCurrentWeekReportTrend, isBeforeCurrentWeek } from '../utils/adminReportStatistics.js';

const STATISTICS_PAGE_SIZE = 100;

export function useAdminReportStatistics() {
  const [statistics, setStatistics] = useState({ statusCounts: {}, weeklyTrend: [] });
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    const controller = new AbortController();
    const now = new Date();

    async function loadStatistics() {
      setLoading(true);
      try {
        const [statusCounts, weeklyReports] = await Promise.all([
          loadStatusCounts(controller.signal),
          loadCurrentWeekReports(controller.signal, now),
        ]);
        setStatistics({
          statusCounts,
          weeklyTrend: createCurrentWeekReportTrend(weeklyReports, now),
        });
        setError('');
      } catch (requestError) {
        if (requestError.code !== 'ERR_CANCELED') {
          setError(requestError.message || 'Không thể tải thống kê báo cáo.');
        }
      } finally {
        if (!controller.signal.aborted) setLoading(false);
      }
    }

    loadStatistics();
    return () => controller.abort();
  }, []);

  return { ...statistics, loading, error };
}

async function loadStatusCounts(signal) {
  const responses = await Promise.all(
    ADMIN_REPORT_STATUSES.map((status) => adminApi.getReports({ status: status.value, page: 0, size: 1 }, signal)),
  );
  return Object.fromEntries(
    ADMIN_REPORT_STATUSES.map((status, index) => [status.value, responses[index].totalElements || 0]),
  );
}

async function loadCurrentWeekReports(signal, now) {
  const reports = [];
  let page = 0;
  let shouldContinue = true;

  while (shouldContinue) {
    const response = await adminApi.getReports({ page, size: STATISTICS_PAGE_SIZE }, signal);
    const content = response.content || [];
    reports.push(...content);

    // API sắp xếp giảm dần khi không lọc trạng thái, nên có thể dừng ngay khi đã đi qua đầu tuần.
    const reachedPreviousWeek = content.some((report) => isBeforeCurrentWeek(report.createdAt, now));
    shouldContinue = !reachedPreviousWeek && page + 1 < (response.totalPages || 0);
    page += 1;
  }

  return reports;
}
