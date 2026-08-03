import { adminApi } from '../../../api/index.js';

/**
 * Service của feature Analytics giữ Page/Hook tách khỏi chi tiết Axios và endpoint trung tâm.
 */
export const userEngagementAnalyticsService = Object.freeze({
  getMonthly: (filters, signal) => adminApi.getUserEngagementMonthly(filters, signal),
  getSummary: (filters, signal) => adminApi.getUserEngagementSummary(filters, signal),
});
