import { useCallback, useEffect, useState } from 'react';
import { adminApi } from '../../../api/index.js';
import { isRequestCanceled, normalizeApiError } from '../../../api/apiError.js';

/** Tải một snapshot đồng bộ cho toàn bộ khối thống kê và hủy request cũ khi đổi bộ lọc. */
export function usePostAnalytics(filters) {
  const [state, setState] = useState({ data: null, loading: true, error: '' });
  const [version, setVersion] = useState(0);
  const retry = useCallback(() => setVersion((value) => value + 1), []);

  useEffect(() => {
    const controller = new AbortController();
    const timer = window.setTimeout(() => {
      setState((current) => ({ ...current, loading: true, error: '' }));
      adminApi.getPostAnalytics(filters, controller.signal)
        .then((data) => setState({ data, loading: false, error: '' }))
        .catch((requestError) => {
          if (isRequestCanceled(requestError)) return;
          setState({ data: null, loading: false, error: normalizeApiError(requestError).message });
        });
    }, 0);
    return () => { window.clearTimeout(timer); controller.abort(); };
  }, [filters, version]);

  return { ...state, retry };
}
