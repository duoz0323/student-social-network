export const discoveryMapInitialState = Object.freeze({
  viewport: null,
  viewportRevision: 0,
  viewportDirty: true,
  markerPhase: 'idle',
  markerRequestId: 0,
  locations: [],
  truncated: false,
  markerError: null,
  selectedLocation: null,
  panelPhase: 'idle',
  postsRequestId: 0,
  posts: [],
  nextCursor: null,
  hasNext: false,
  paginationPhase: 'idle',
  panelError: null,
  loadMoreError: null,
  geolocationPhase: 'idle',
  geolocationMessage: '',
  userCoordinates: null,
});

export function uniqueMapPosts(posts) {
  const seen = new Set();
  return posts.filter((post) => {
    const key = String(post.id ?? post.postId);
    if (!key || key === 'undefined' || seen.has(key)) return false;
    seen.add(key);
    return true;
  });
}

function resetPanel(state) {
  return {
    ...state,
    selectedLocation: null,
    panelPhase: 'idle',
    posts: [],
    nextCursor: null,
    hasNext: false,
    paginationPhase: 'idle',
    panelError: null,
    loadMoreError: null,
  };
}

/** State machine của Map loại bỏ response cũ bằng request ID và giữ cursor hoàn toàn opaque. */
export function discoveryMapReducer(state, action) {
  switch (action.type) {
    case 'VIEWPORT_CHANGED':
      return {
        ...state,
        viewport: action.viewport,
        viewportRevision: state.viewportRevision + 1,
        viewportDirty: true,
      };
    case 'MARKERS_REQUESTED':
      return resetPanel({
        ...state,
        markerPhase: 'loading',
        markerRequestId: action.requestId,
        markerError: null,
        // Viewport hiện tại đã được gửi; chỉ mở lại Search nếu người dùng pan/zoom sang viewport mới.
        viewportDirty: false,
      });
    case 'MARKERS_SUCCEEDED':
      if (action.requestId !== state.markerRequestId) return state;
      return {
        ...state,
        markerPhase: action.locations.length ? 'success' : 'empty',
        locations: action.locations,
        truncated: Boolean(action.truncated),
        markerError: null,
        viewportDirty: action.viewportRevision !== state.viewportRevision,
      };
    case 'MARKERS_FAILED':
      if (action.requestId !== state.markerRequestId) return state;
      return { ...state, markerPhase: 'error', markerError: action.error };
    case 'LOCATION_SELECTED':
      return {
        ...resetPanel(state),
        selectedLocation: action.location,
        panelPhase: 'loading',
      };
    case 'LOCATION_CLOSED':
      return resetPanel(state);
    case 'POSTS_REQUESTED':
      return {
        ...state,
        postsRequestId: action.requestId,
        panelPhase: 'loading',
        paginationPhase: 'idle',
        posts: [],
        nextCursor: null,
        hasNext: false,
        panelError: null,
        loadMoreError: null,
      };
    case 'POSTS_SUCCEEDED': {
      if (action.requestId !== state.postsRequestId) return state;
      const posts = uniqueMapPosts(action.posts);
      const hasNext = Boolean(action.hasNext && action.nextCursor);
      return {
        ...state,
        panelPhase: posts.length ? 'success' : 'empty',
        posts,
        nextCursor: action.nextCursor ?? null,
        hasNext,
        paginationPhase: hasNext ? 'idle' : 'end',
      };
    }
    case 'POSTS_FAILED':
      if (action.requestId !== state.postsRequestId) return state;
      return { ...state, panelPhase: 'error', panelError: action.error };
    case 'LOAD_MORE_REQUESTED':
      return {
        ...state,
        postsRequestId: action.requestId,
        paginationPhase: 'loading',
        loadMoreError: null,
      };
    case 'LOAD_MORE_SUCCEEDED': {
      if (action.requestId !== state.postsRequestId) return state;
      const posts = uniqueMapPosts([...state.posts, ...action.posts]);
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
      if (action.requestId !== state.postsRequestId) return state;
      return {
        ...state,
        paginationPhase: action.retryable ? 'error' : 'end',
        hasNext: action.retryable ? state.hasNext : false,
        loadMoreError: action.error,
      };
    case 'GEOLOCATION_REQUESTED':
      return { ...state, geolocationPhase: 'loading', geolocationMessage: '' };
    case 'GEOLOCATION_SUCCEEDED':
      return {
        ...state,
        geolocationPhase: 'success',
        geolocationMessage: 'Đã chuyển bản đồ tới vị trí hiện tại. Hãy tìm lại trong khu vực này.',
        userCoordinates: action.coordinates,
        viewportDirty: true,
      };
    case 'GEOLOCATION_FAILED':
      return {
        ...state,
        geolocationPhase: 'error',
        geolocationMessage: action.error,
      };
    default:
      return state;
  }
}

export function canLoadMoreMapPosts(state, requestInFlight) {
  return Boolean(
    state.selectedLocation
    && state.panelPhase === 'success'
    && state.paginationPhase === 'idle'
    && state.hasNext
    && state.nextCursor
    && !requestInFlight,
  );
}
