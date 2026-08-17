import { USER_ENDPOINTS } from '../../../api/apiEndpoints.js';
import { httpClient } from '../../../api/httpClient.js';
import { requestData } from '../../../api/requestData.js';

// Account Standing luôn lấy từ Backend; Frontend không tự suy diễn từ Notification.
export const accountApi = Object.freeze({
  getStanding: (signal) => requestData(httpClient.get(USER_ENDPOINTS.accountStanding, { signal })),
});
