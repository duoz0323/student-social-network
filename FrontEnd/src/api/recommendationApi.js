import { DISCOVERY_ENDPOINTS } from './apiEndpoints.js';
import { httpClient } from './httpClient.js';
import { compactParams, requestData } from './requestData.js';

/** API Recommendation chỉ nhận tham số phân trang; tiêu chí gợi ý do Backend quyết định từ JWT. */
export const recommendationApi = Object.freeze({
  getStudents: (params, signal) => requestData(
    httpClient.get(DISCOVERY_ENDPOINTS.studentRecommendations, {
      params: compactParams(params),
      signal,
    }),
  ),
});
