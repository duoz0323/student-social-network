import { DISCOVERY_ENDPOINTS } from './apiEndpoints.js';
import { httpClient } from './httpClient.js';
import { compactParams, requestData } from './requestData.js';

export const feedApi = Object.freeze({
  getForYou: (params, signal) => requestData(
    httpClient.get(DISCOVERY_ENDPOINTS.feedForYou, { params: compactParams(params), signal }),
  ),
  getFollowing: (params, signal) => requestData(
    httpClient.get(DISCOVERY_ENDPOINTS.feedFollowing, { params: compactParams(params), signal }),
  ),
});
