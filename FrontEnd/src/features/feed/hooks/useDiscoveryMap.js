import { useCallback, useEffect, useReducer, useRef } from 'react';
import { isRequestCanceled } from '../../../api/apiError.js';
import { toPostView } from '../../post/utils/postViewModel.js';
import { DISCOVERY_MAP_CONFIG } from '../config/discoveryMapConfig.js';
import { discoveryMapApi } from '../services/discoveryMapApi.js';
import { getCurrentCoordinates } from '../utils/browserGeolocation.js';
import { normalizeDiscoveryMapBounds } from '../utils/discoveryMapBounds.js';
import {
  canLoadMoreMapPosts,
  discoveryMapInitialState,
  discoveryMapReducer,
} from '../utils/discoveryMapState.js';

function normalizeLocation(location = {}) {
  return {
    ...location,
    locationId: Number(location.locationId),
    latitude: Number(location.latitude),
    longitude: Number(location.longitude),
    postCount: Math.max(0, Number(location.postCount) || 0),
  };
}

/** Điều phối marker, panel cursor và GPS bằng request độc lập để đổi viewport/marker không nhận response cũ. */
export function useDiscoveryMap({ active }) {
  const [state, dispatch] = useReducer(discoveryMapReducer, discoveryMapInitialState);
  const stateRef = useRef(state);
  const activeRef = useRef(active);
  const markerRequestRef = useRef(null);
  const postsRequestRef = useRef(null);
  const markerSequenceRef = useRef(0);
  const postsSequenceRef = useRef(0);
  const geolocationInFlightRef = useRef(false);

  useEffect(() => {
    stateRef.current = state;
  }, [state]);

  const cancelMarkerRequest = useCallback(() => {
    markerRequestRef.current?.abort();
    markerRequestRef.current = null;
  }, []);

  const cancelPostsRequest = useCallback(() => {
    postsRequestRef.current?.abort();
    postsRequestRef.current = null;
  }, []);

  useEffect(() => {
    activeRef.current = active;
    if (!active) {
      cancelMarkerRequest();
      cancelPostsRequest();
    }
  }, [active, cancelMarkerRequest, cancelPostsRequest]);

  useEffect(() => () => {
    activeRef.current = false;
    cancelMarkerRequest();
    cancelPostsRequest();
  }, [cancelMarkerRequest, cancelPostsRequest]);

  const updateViewport = useCallback((bounds) => {
    const viewport = normalizeDiscoveryMapBounds(bounds);
    if (viewport) dispatch({ type: 'VIEWPORT_CHANGED', viewport });
  }, []);

  const searchViewport = useCallback(async () => {
    const current = stateRef.current;
    const viewport = normalizeDiscoveryMapBounds(current.viewport);
    if (!activeRef.current || !viewport) return;

    cancelMarkerRequest();
    cancelPostsRequest();
    const controller = new AbortController();
    const requestId = ++markerSequenceRef.current;
    const viewportRevision = current.viewportRevision;
    markerRequestRef.current = controller;
    dispatch({ type: 'MARKERS_REQUESTED', requestId });
    try {
      const response = await discoveryMapApi.getMapLocations(viewport, controller.signal);
      dispatch({
        type: 'MARKERS_SUCCEEDED',
        requestId,
        viewportRevision,
        locations: (response.locations ?? []).map(normalizeLocation),
        truncated: response.truncated,
      });
    } catch (error) {
      if (!isRequestCanceled(error)) {
        dispatch({
          type: 'MARKERS_FAILED',
          requestId,
          error: error.message || 'Không thể tải địa điểm trong khu vực này.',
        });
      }
    } finally {
      if (markerRequestRef.current === controller) markerRequestRef.current = null;
    }
  }, [cancelMarkerRequest, cancelPostsRequest]);

  const loadFirstPosts = useCallback(async (location) => {
    cancelPostsRequest();
    const controller = new AbortController();
    const requestId = ++postsSequenceRef.current;
    postsRequestRef.current = controller;
    dispatch({ type: 'POSTS_REQUESTED', requestId });
    try {
      const response = await discoveryMapApi.getMapLocationPosts({
        locationId: location.locationId,
        limit: DISCOVERY_MAP_CONFIG.locationPostsLimit,
      }, controller.signal);
      dispatch({
        type: 'POSTS_SUCCEEDED',
        requestId,
        posts: (response.content ?? []).map(toPostView),
        nextCursor: response.nextCursor,
        hasNext: response.hasNext,
      });
    } catch (error) {
      if (!isRequestCanceled(error)) {
        dispatch({ type: 'POSTS_FAILED', requestId, error: error.message || 'Không thể tải bài viết tại địa điểm.' });
      }
    } finally {
      if (postsRequestRef.current === controller) postsRequestRef.current = null;
    }
  }, [cancelPostsRequest]);

  const selectLocation = useCallback((location) => {
    if (!activeRef.current || !location?.locationId) return;
    dispatch({ type: 'LOCATION_SELECTED', location });
    void loadFirstPosts(location);
  }, [loadFirstPosts]);

  const closeLocation = useCallback(() => {
    cancelPostsRequest();
    dispatch({ type: 'LOCATION_CLOSED' });
  }, [cancelPostsRequest]);

  const loadMore = useCallback(async () => {
    const current = stateRef.current;
    if (!canLoadMoreMapPosts(current, Boolean(postsRequestRef.current))) return;
    const controller = new AbortController();
    const requestId = ++postsSequenceRef.current;
    postsRequestRef.current = controller;
    dispatch({ type: 'LOAD_MORE_REQUESTED', requestId });
    try {
      const response = await discoveryMapApi.getMapLocationPosts({
        locationId: current.selectedLocation.locationId,
        limit: DISCOVERY_MAP_CONFIG.locationPostsLimit,
        cursor: current.nextCursor,
      }, controller.signal);
      dispatch({
        type: 'LOAD_MORE_SUCCEEDED',
        requestId,
        posts: (response.content ?? []).map(toPostView),
        nextCursor: response.nextCursor,
        hasNext: response.hasNext,
      });
    } catch (error) {
      if (!isRequestCanceled(error)) {
        dispatch({
          type: 'LOAD_MORE_FAILED',
          requestId,
          retryable: error.code !== 'INVALID_CURSOR',
          error: error.message || 'Không thể tải thêm bài viết.',
        });
      }
    } finally {
      if (postsRequestRef.current === controller) postsRequestRef.current = null;
    }
  }, []);

  const requestMyLocation = useCallback(async () => {
    if (!activeRef.current || geolocationInFlightRef.current) return;
    geolocationInFlightRef.current = true;
    dispatch({ type: 'GEOLOCATION_REQUESTED' });
    try {
      const coordinates = await getCurrentCoordinates(globalThis.navigator?.geolocation, { fresh: true });
      if (activeRef.current) dispatch({ type: 'GEOLOCATION_SUCCEEDED', coordinates });
    } catch (error) {
      if (activeRef.current) {
        dispatch({
          type: 'GEOLOCATION_FAILED',
          error: error.message || 'Không thể xác định vị trí hiện tại.',
        });
      }
    } finally {
      geolocationInFlightRef.current = false;
    }
  }, []);

  return {
    ...state,
    updateViewport,
    searchViewport,
    selectLocation,
    closeLocation,
    retryLocationPosts: () => stateRef.current.selectedLocation && loadFirstPosts(stateRef.current.selectedLocation),
    loadMore,
    retryLoadMore: loadMore,
    requestMyLocation,
  };
}
