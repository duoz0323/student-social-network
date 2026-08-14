export const NEARBY_RADII_KM = Object.freeze([1, 3, 5, 10, 20]);
export const DEFAULT_NEARBY_RADIUS_KM = 5;

export const nearbyInitialState = Object.freeze({
  phase: 'initial',
  paginationPhase: 'idle',
  coordinates: null,
  radiusKm: DEFAULT_NEARBY_RADIUS_KM,
  posts: [],
  nextCursor: null,
  hasNext: false,
  error: null,
  loadMoreError: null,
  requestId: 0,
});

function uniqueNearbyPosts(posts) {
  const seen = new Set();
  return posts.filter((post) => {
    const key = String(post.id ?? post.postId);
    if (seen.has(key)) return false;
    seen.add(key);
    return true;
  });
}

/** State machine giữ các trạng thái Nearby loại trừ nhau và bỏ qua response đã lỗi thời. */
export function nearbyDiscoveryReducer(state, action) {
  switch (action.type) {
    case 'LOCATION_REQUESTED':
      return {
        ...state,
        phase: 'requesting-location',
        paginationPhase: 'idle',
        coordinates: null,
        posts: [],
        nextCursor: null,
        hasNext: false,
        error: null,
        loadMoreError: null,
      };
    case 'LOCATION_READY':
      return { ...state, phase: 'location-ready', coordinates: action.coordinates, error: null };
    case 'LOCATION_FAILED':
      return {
        ...state,
        phase: action.phase,
        paginationPhase: 'idle',
        coordinates: null,
        posts: [],
        nextCursor: null,
        hasNext: false,
        error: action.error,
        loadMoreError: null,
      };
    case 'RADIUS_CHANGED':
      return {
        ...state,
        radiusKm: action.radiusKm,
        posts: [],
        nextCursor: null,
        hasNext: false,
        error: null,
        loadMoreError: null,
        paginationPhase: 'idle',
      };
    case 'FIRST_PAGE_REQUESTED':
      return {
        ...state,
        phase: 'loading',
        paginationPhase: 'idle',
        posts: [],
        nextCursor: null,
        hasNext: false,
        error: null,
        loadMoreError: null,
        requestId: action.requestId,
      };
    case 'FIRST_PAGE_SUCCEEDED': {
      if (action.requestId !== state.requestId) return state;
      const posts = uniqueNearbyPosts(action.posts);
      const hasNext = Boolean(action.hasNext && action.nextCursor);
      return {
        ...state,
        phase: posts.length ? 'success' : 'empty',
        paginationPhase: hasNext ? 'idle' : 'end',
        posts,
        nextCursor: action.nextCursor ?? null,
        hasNext,
      };
    }
    case 'FIRST_PAGE_FAILED':
      if (action.requestId !== state.requestId) return state;
      return { ...state, phase: 'api-error', error: action.error };
    case 'REQUEST_CANCELED':
      if (action.requestId !== state.requestId) return state;
      return {
        ...state,
        phase: state.coordinates ? 'location-ready' : 'initial',
        paginationPhase: 'idle',
        requestId: -1,
      };
    case 'LOAD_MORE_REQUESTED':
      return {
        ...state,
        paginationPhase: 'loading',
        loadMoreError: null,
        requestId: action.requestId,
      };
    case 'LOAD_MORE_SUCCEEDED': {
      if (action.requestId !== state.requestId) return state;
      const posts = uniqueNearbyPosts([...state.posts, ...action.posts]);
      const hasNext = Boolean(action.hasNext && action.nextCursor);
      return {
        ...state,
        posts,
        nextCursor: action.nextCursor ?? null,
        hasNext,
        paginationPhase: hasNext ? 'idle' : 'end',
      };
    }
    case 'LOAD_MORE_FAILED':
      if (action.requestId !== state.requestId) return state;
      return {
        ...state,
        hasNext: action.retryable ? state.hasNext : false,
        paginationPhase: action.retryable ? 'error' : 'end',
        loadMoreError: action.error,
      };
    default:
      return state;
  }
}

export function shouldRequestNearbyLocation({ active, phase, inFlight }) {
  return Boolean(active && phase === 'initial' && !inFlight);
}

export function canLoadMoreNearby({ active, requestInFlight, phase, paginationPhase, hasNext, nextCursor }) {
  return Boolean(active && !requestInFlight && phase === 'success' && paginationPhase === 'idle' && hasNext && nextCursor);
}

export function formatNearbyDistance(distanceMeters) {
  const meters = Math.max(0, Math.round(Number(distanceMeters) || 0));
  if (meters < 1000) return `${meters} m`;
  const kilometers = Math.round(meters / 100) / 10;
  return `${kilometers.toFixed(kilometers % 1 === 0 ? 0 : 1)} km`;
}
