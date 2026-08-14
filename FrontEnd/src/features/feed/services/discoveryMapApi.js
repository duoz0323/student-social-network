import { httpClient } from '../../../api/httpClient.js';
import { requestData } from '../../../api/requestData.js';
import { createDiscoveryMapApi } from './discoveryMapApiCore.js';

const api = createDiscoveryMapApi({
  get: (...args) => httpClient.get(...args),
  unwrap: requestData,
});

// Public service giữ tên theo contract task và luôn đi qua Axios/interceptor chung.
export const getMapLocations = api.getMapLocations;
export const getMapLocationPosts = api.getMapLocationPosts;
export const discoveryMapApi = api;
