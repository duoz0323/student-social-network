import { DISCOVERY_ENDPOINTS } from '../../../api/apiEndpoints.js';

function compactParams(params = {}) {
  return Object.fromEntries(
    Object.entries(params).filter(([, value]) => value !== undefined && value !== null && value !== ''),
  );
}

/** Factory thuần giúp kiểm thử contract service mà không phụ thuộc môi trường Vite hoặc Axios thật. */
export function createDiscoveryMapApi({ get, unwrap }) {
  return Object.freeze({
    getMapLocations: (params, signal) => unwrap(get(DISCOVERY_ENDPOINTS.mapLocations, {
      params: compactParams(params),
      signal,
    })),
    getMapLocationPosts: ({ locationId, limit, cursor }, signal) => unwrap(get(
      DISCOVERY_ENDPOINTS.mapLocationPosts(locationId),
      { params: compactParams({ limit, cursor }), signal },
    )),
  });
}
