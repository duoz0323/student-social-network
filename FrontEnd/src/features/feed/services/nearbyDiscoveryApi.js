import { DISCOVERY_ENDPOINTS } from '../../../api/apiEndpoints.js';
import { httpClient } from '../../../api/httpClient.js';
import { compactParams, requestData } from '../../../api/requestData.js';

export const nearbyDiscoveryApi = Object.freeze({
  getNearby: (params, signal) => requestData(
    httpClient.get(DISCOVERY_ENDPOINTS.nearby, { params: compactParams(params), signal }),
  ),
});

