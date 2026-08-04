/* eslint-disable react-refresh/only-export-components */
import { createContext, useCallback, useContext, useEffect, useMemo, useRef, useState } from 'react';
import { realtimeSocket } from '../config/realtimeSocket.js';
import { useAuth } from '../features/auth/hooks/useAuth.js';

const RealtimeContext = createContext(null);

/** RealtimeProvider là owner duy nhất của lifecycle WebSocket/STOMP trong một tab. */
export function RealtimeProvider({ children }) {
  const auth = useAuth();
  const { refreshSession } = auth;
  const [connected, setConnected] = useState(false);
  const refreshInFlightRef = useRef(false);
  const eligible = Boolean(
    auth.isAuthenticated
    && auth.profileCompleted
    && auth.authStatus !== 'BLOCKED',
  );

  const handleAuthenticationError = useCallback(async () => {
    if (refreshInFlightRef.current) return;
    refreshInFlightRef.current = true;
    try {
      await refreshSession();
      await realtimeSocket.reconnect();
    } catch {
      await realtimeSocket.deactivate();
    } finally {
      refreshInFlightRef.current = false;
    }
  }, [refreshSession]);

  useEffect(() => realtimeSocket.addConnectionListener(setConnected), []);

  useEffect(() => {
    realtimeSocket.setLifecycleHandlers({ onAuthenticationError: handleAuthenticationError });
    return () => realtimeSocket.setLifecycleHandlers();
  }, [handleAuthenticationError]);

  useEffect(() => {
    if (eligible) realtimeSocket.activate();
    else realtimeSocket.deactivate();

    return () => {
      realtimeSocket.deactivate();
    };
  }, [eligible]);

  const subscribe = useCallback(
    (destination, callback) => realtimeSocket.subscribe(destination, callback),
    [],
  );
  const unsubscribe = useCallback(
    (tokenOrDestination, callback) => realtimeSocket.unsubscribe(tokenOrDestination, callback),
    [],
  );
  const reconnect = useCallback(() => realtimeSocket.reconnect(), []);
  const send = useCallback(
    (destination, payload) => realtimeSocket.send(destination, payload),
    [],
  );

  const value = useMemo(() => ({
    connected,
    subscribe,
    unsubscribe,
    send,
    reconnect,
  }), [connected, reconnect, send, subscribe, unsubscribe]);

  return <RealtimeContext.Provider value={value}>{children}</RealtimeContext.Provider>;
}

export function useRealtime() {
  const context = useContext(RealtimeContext);
  if (!context) throw new Error('useRealtime phải được sử dụng bên trong RealtimeProvider.');
  return context;
}
