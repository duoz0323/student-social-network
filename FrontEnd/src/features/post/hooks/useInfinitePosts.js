import { useCallback, useEffect, useRef, useState } from 'react';
import { postListCache } from './postListCache.js';

function uniquePosts(posts) {
  const seen = new Set();
  return posts.filter((post) => {
    // Timeline activity có thể chứa cùng một bài gốc do nhiều người Repost nên không khử trùng chỉ bằng postId.
    const id = String(post.feedItemKey ?? post.id ?? post.postId);
    if (seen.has(id)) return false;
    seen.add(id);
    return true;
  });
}

/**
 * Quản lý cursor pagination, cache theo danh sách và chỉ cho một request tải tiếp chạy cùng lúc.
 */
export function useInfinitePosts({ cacheKey, request, normalizePost, limit = 10, enabled = true }) {
  const cached = postListCache.get(cacheKey);
  const [posts, setPosts] = useState(cached?.posts ?? []);
  const [nextCursor, setNextCursor] = useState(cached?.nextCursor ?? null);
  const [hasNext, setHasNext] = useState(cached?.hasNext ?? true);
  const [initialLoading, setInitialLoading] = useState(!cached);
  const [loadingMore, setLoadingMore] = useState(false);
  const [initialError, setInitialError] = useState('');
  const [loadMoreError, setLoadMoreError] = useState('');
  const inFlight = useRef(false);
  const observer = useRef(null);
  const requestRef = useRef(request);
  const normalizeRef = useRef(normalizePost);

  useEffect(() => {
    requestRef.current = request;
    normalizeRef.current = normalizePost;
  }, [normalizePost, request]);

  const loadPage = useCallback(async ({ cursor = null, replace = false } = {}) => {
    if (inFlight.current) return;
    inFlight.current = true;
    if (replace) {
      setInitialLoading(true);
      setInitialError('');
    } else {
      setLoadingMore(true);
      setLoadMoreError('');
    }

    try {
      const response = await requestRef.current({ limit, cursor });
      const incoming = (response.content ?? []).map(normalizeRef.current);
      setPosts((current) => {
        const merged = uniquePosts(replace ? incoming : [...current, ...incoming]);
        postListCache.set(cacheKey, {
          posts: merged,
          nextCursor: response.nextCursor ?? null,
          hasNext: Boolean(response.hasNext),
        });
        return merged;
      });
      setNextCursor(response.nextCursor ?? null);
      setHasNext(Boolean(response.hasNext));
    } catch (error) {
      if (replace) setInitialError(error.message);
      else setLoadMoreError(error.message);
    } finally {
      inFlight.current = false;
      setInitialLoading(false);
      setLoadingMore(false);
    }
  }, [cacheKey, limit]);

  useEffect(() => {
    if (!enabled) return undefined;
    const saved = postListCache.get(cacheKey);
    let stateTimer;
    if (saved) {
      stateTimer = setTimeout(() => {
        setPosts(saved.posts);
        setNextCursor(saved.nextCursor);
        setHasNext(saved.hasNext);
        setInitialLoading(false);
      }, 0);
    } else {
      stateTimer = setTimeout(() => {
        setPosts([]);
        setNextCursor(null);
        setHasNext(true);
        loadPage({ replace: true });
      }, 0);
    }
    return () => {
      clearTimeout(stateTimer);
    };
  }, [cacheKey, enabled, loadPage]);

  const loadMore = useCallback(() => {
    if (hasNext && nextCursor && !inFlight.current) {
      loadPage({ cursor: nextCursor });
    }
  }, [hasNext, loadPage, nextCursor]);

  const sentinelRef = useCallback((node) => {
    observer.current?.disconnect();
    if (!node || !hasNext || !nextCursor) return;
    observer.current = new IntersectionObserver(
      (entries) => {
        if (entries[0]?.isIntersecting) loadMore();
      },
      { rootMargin: '400px 0px' },
    );
    observer.current.observe(node);
  }, [hasNext, loadMore, nextCursor]);

  const [refreshing, setRefreshing] = useState(false);

  const reload = useCallback(() => {
    postListCache.delete(cacheKey);
    setPosts([]);
    setNextCursor(null);
    setHasNext(true);
    loadPage({ replace: true });
  }, [cacheKey, loadPage]);

  const refresh = useCallback(async () => {
    if (inFlight.current) return;
    setRefreshing(true);
    try {
      postListCache.delete(cacheKey);
      await loadPage({ replace: true });
    } finally {
      setRefreshing(false);
    }
  }, [cacheKey, loadPage]);

  const removePost = useCallback((postId) => {
    setPosts((current) => {
      const next = current.filter((post) => String(post.id) !== String(postId));
      const cachedValue = postListCache.get(cacheKey);
      if (cachedValue) postListCache.set(cacheKey, { ...cachedValue, posts: next });
      return next;
    });
  }, [cacheKey]);

  return {
    posts, initialLoading, loadingMore, refreshing, initialError, loadMoreError,
    hasNext, sentinelRef, reload, refresh, retryLoadMore: loadMore, removePost,
  };
}
