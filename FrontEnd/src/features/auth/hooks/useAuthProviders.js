import { useCallback, useEffect, useRef, useState } from 'react';
import { authProviderService } from '../services/authProviderService.js';
import { getAuthProviderErrorMessage } from '../utils/authProviderErrorMapper.js';

export function useAuthProviders() {
  const mountedRef = useRef(false);
  const controllerRef = useRef(null);
  const [state, setState] = useState({ methods: [], isLoading: true, error: '' });

  const load = useCallback(async () => {
    if (!mountedRef.current) return null;
    controllerRef.current?.abort();
    const controller = new AbortController();
    controllerRef.current = controller;
    if (mountedRef.current) setState((current) => ({ ...current, isLoading: true, error: '' }));
    try {
      const methods = await authProviderService.list(controller.signal);
      if (mountedRef.current) setState({ methods, isLoading: false, error: '' });
      return methods;
    } catch (error) {
      if (error?.code === 'ERR_CANCELED') return null;
      if (mountedRef.current) setState((current) => ({ ...current, isLoading: false, error: getAuthProviderErrorMessage(error) }));
      throw error;
    }
  }, []);

  useEffect(() => {
    mountedRef.current = true;
    void Promise.resolve().then(load).catch(() => {});
    return () => { mountedRef.current = false; controllerRef.current?.abort(); };
  }, [load]);

  return { ...state, refetch: load };
}
