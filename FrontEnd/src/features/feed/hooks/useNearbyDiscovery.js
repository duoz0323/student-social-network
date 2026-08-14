import { useCallback, useEffect, useReducer, useRef } from 'react';
import { isRequestCanceled } from '../../../api/apiError.js';
import { toPostView } from '../../post/utils/postViewModel.js';
import { nearbyDiscoveryApi } from '../services/nearbyDiscoveryApi.js';
import { getCurrentCoordinates } from '../utils/browserGeolocation.js';
import {
  NEARBY_RADII_KM,
  canLoadMoreNearby,
  nearbyDiscoveryReducer,
  nearbyInitialState,
  shouldRequestNearbyLocation,
} from '../utils/nearbyDiscoveryState.js';

function normalizeNearbyItem(item = {}) {
  return {
    ...toPostView(item.post ?? {}),
    nearbyDistanceMeters: Math.max(0, Math.round(Number(item.distanceMeters) || 0)),
  };
}

/** Điều phối Geolocation và cursor Nearby, đồng thời hủy hoặc bỏ qua mọi response đã lỗi thời. */
export function useNearbyDiscovery({ active }) {
  const [state, dispatch] = useReducer(nearbyDiscoveryReducer, nearbyInitialState);
  const stateRef = useRef(state);
  const activeRef = useRef(active);
  const apiRequestRef = useRef(null);
  const apiSequenceRef = useRef(0);
  const geoSequenceRef = useRef(0);
  const geoInFlightRef = useRef(false);

  useEffect(() => {
    stateRef.current = state;
  }, [state]);

  const cancelApiRequest = useCallback(() => {
    const current = apiRequestRef.current;
    if (!current) return;
    apiRequestRef.current = null;
    current.controller.abort();
    dispatch({ type: 'REQUEST_CANCELED', requestId: current.requestId });
  }, []);

  const loadFirstPage = useCallback(async (coordinates, radiusKm) => {
    cancelApiRequest();
    const requestId = ++apiSequenceRef.current;
    const controller = new AbortController();
    apiRequestRef.current = { controller, requestId };
    dispatch({ type: 'FIRST_PAGE_REQUESTED', requestId });

    try {
      const response = await nearbyDiscoveryApi.getNearby({
        ...coordinates,
        radiusKm,
        limit: 10,
      }, controller.signal);
      dispatch({
        type: 'FIRST_PAGE_SUCCEEDED',
        requestId,
        posts: (response.content ?? []).map(normalizeNearbyItem),
        nextCursor: response.nextCursor,
        hasNext: response.hasNext,
      });
    } catch (error) {
      if (!isRequestCanceled(error)) {
        dispatch({ type: 'FIRST_PAGE_FAILED', requestId, error: error.message || 'Không thể tải bài viết gần bạn.' });
      }
    } finally {
      if (apiRequestRef.current?.requestId === requestId) apiRequestRef.current = null;
    }
  }, [cancelApiRequest]);

  const requestLocation = useCallback(async ({ fresh = false } = {}) => {
    if (geoInFlightRef.current) return;
    geoInFlightRef.current = true;
    const geoRequestId = ++geoSequenceRef.current;
    cancelApiRequest();
    dispatch({ type: 'LOCATION_REQUESTED' });

    try {
      const coordinates = await getCurrentCoordinates(globalThis.navigator?.geolocation, { fresh });
      if (geoRequestId !== geoSequenceRef.current) return;
      dispatch({ type: 'LOCATION_READY', coordinates });
    } catch (error) {
      if (geoRequestId !== geoSequenceRef.current) return;
      dispatch({
        type: 'LOCATION_FAILED',
        phase: error.kind ?? 'unavailable',
        error: error.message || 'Không thể xác định vị trí hiện tại.',
      });
    } finally {
      if (geoRequestId === geoSequenceRef.current) geoInFlightRef.current = false;
    }
  }, [cancelApiRequest]);

  useEffect(() => {
    activeRef.current = active;
    if (!active) {
      cancelApiRequest();
      return;
    }
    if (shouldRequestNearbyLocation({ active, phase: state.phase, inFlight: geoInFlightRef.current })) {
      void requestLocation();
    }
    if (state.phase === 'location-ready' && state.coordinates) {
      void loadFirstPage(state.coordinates, state.radiusKm);
    }
  }, [active, cancelApiRequest, loadFirstPage, requestLocation, state.coordinates, state.phase, state.radiusKm]);

  useEffect(() => () => {
    activeRef.current = false;
    apiRequestRef.current?.controller.abort();
    apiRequestRef.current = null;
    geoSequenceRef.current += 1;
  }, []);

  const changeRadius = useCallback((radiusKm) => {
    const nextRadius = Number(radiusKm);
    if (!NEARBY_RADII_KM.includes(nextRadius) || nextRadius === stateRef.current.radiusKm) return;
    dispatch({ type: 'RADIUS_CHANGED', radiusKm: nextRadius });
    if (stateRef.current.coordinates && activeRef.current) {
      void loadFirstPage(stateRef.current.coordinates, nextRadius);
    }
  }, [loadFirstPage]);

  const loadMore = useCallback(async () => {
    const current = stateRef.current;
    if (!canLoadMoreNearby({
      active: activeRef.current,
      requestInFlight: Boolean(apiRequestRef.current),
      phase: current.phase,
      paginationPhase: current.paginationPhase,
      hasNext: current.hasNext,
      nextCursor: current.nextCursor,
    })) return;

    const requestId = ++apiSequenceRef.current;
    const controller = new AbortController();
    apiRequestRef.current = { controller, requestId };
    dispatch({ type: 'LOAD_MORE_REQUESTED', requestId });
    try {
      const response = await nearbyDiscoveryApi.getNearby({
        ...current.coordinates,
        radiusKm: current.radiusKm,
        limit: 10,
        cursor: current.nextCursor,
      }, controller.signal);
      dispatch({
        type: 'LOAD_MORE_SUCCEEDED',
        requestId,
        posts: (response.content ?? []).map(normalizeNearbyItem),
        nextCursor: response.nextCursor,
        hasNext: response.hasNext,
      });
    } catch (error) {
      if (!isRequestCanceled(error)) {
        dispatch({
          type: 'LOAD_MORE_FAILED',
          requestId,
          error: error.message || 'Không thể tải thêm bài viết.',
          retryable: error.code !== 'INVALID_CURSOR',
        });
      }
    } finally {
      if (apiRequestRef.current?.requestId === requestId) apiRequestRef.current = null;
    }
  }, []);

  const retry = useCallback(() => {
    const current = stateRef.current;
    if (current.phase === 'api-error' && current.coordinates) {
      void loadFirstPage(current.coordinates, current.radiusKm);
      return;
    }
    if (['permission-denied', 'unavailable', 'timeout'].includes(current.phase)) {
      void requestLocation({ fresh: true });
    }
  }, [loadFirstPage, requestLocation]);

  return {
    ...state,
    changeRadius,
    refreshLocation: () => requestLocation({ fresh: true }),
    retry,
    loadMore,
    retryLoadMore: loadMore,
  };
}
