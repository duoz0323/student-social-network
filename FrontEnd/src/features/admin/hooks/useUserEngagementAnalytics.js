import { useCallback, useEffect, useState } from 'react';
import { userEngagementAnalyticsService } from '../services/userEngagementAnalyticsService.js';
import {
  normalizeMonthlyAnalytics,
  normalizeSummaryAnalytics,
} from '../utils/userEngagementAnalytics.js';

/**
 * Hook tải đồng thời chuỗi tháng và summary tháng kết thúc, đồng thời hủy request khi bộ lọc đổi.
 */
export function useUserEngagementAnalytics(filters) {
  const [state, setState] = useState({ monthly: null, summary: null, loading: true, error: '' });
  const [revision, setRevision] = useState(0);
  const { fromMonth, inactiveDays, toMonth } = filters;

  const retry = useCallback(() => setRevision((current) => current + 1), []);

  useEffect(() => {
    const controller = new AbortController();
    const requestFilters = { fromMonth, toMonth, inactiveDays };
    const timer = window.setTimeout(() => {
      setState((current) => ({ ...current, loading: true, error: '' }));
      Promise.all([
        userEngagementAnalyticsService.getMonthly(requestFilters, controller.signal),
        userEngagementAnalyticsService.getSummary(
          { month: toMonth, inactiveDays },
          controller.signal,
        ),
      ])
        .then(([monthly, summary]) => {
          if (!controller.signal.aborted) {
            setState({
              monthly: normalizeMonthlyAnalytics(monthly),
              summary: normalizeSummaryAnalytics(summary),
              loading: false,
              error: '',
            });
          }
        })
        .catch((requestError) => {
          if (!controller.signal.aborted && requestError.code !== 'ERR_CANCELED') {
            setState((current) => ({
              ...current,
              loading: false,
              error: requestError.message || 'Không thể tải thống kê hoạt động người dùng.',
            }));
          }
        });
    }, 0);

    return () => {
      window.clearTimeout(timer);
      controller.abort();
    };
  }, [fromMonth, inactiveDays, toMonth, revision]);

  return { ...state, retry };
}
