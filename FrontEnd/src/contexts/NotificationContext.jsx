/* eslint-disable react-refresh/only-export-components */
import {
  createContext,
  useCallback,
  useContext,
  useEffect,
  useMemo,
  useRef,
  useState,
} from 'react';
import { socialApi } from '../api/socialApi.js';
import { isRequestCanceled } from '../api/apiError.js';
import { useAuth } from '../features/auth/hooks/useAuth.js';
import { useRealtime } from './RealtimeContext.jsx';
import {
  applyCreatedNotificationEvent,
  canUseNotificationRealtime,
  markAllNotificationsReadState,
  markNotificationReadState,
  mergeUniqueNotifications,
  NOTIFICATION_FIRST_PAGE_SIZE,
  rememberEventId,
  removeNotificationState,
  shouldPollNotificationCount,
} from '../features/notification/utils/notificationState.js';

const POLLING_INTERVAL_MS = 30_000;
const NOTIFICATION_DESTINATION = '/user/queue/notifications';
const NotificationContext = createContext(null);

const initialState = {
  notifications: [],
  unreadCount: 0,
  socketConnected: false,
  loading: false,
  loadingMore: false,
  initialized: false,
  page: -1,
  totalPages: 0,
  last: true,
  error: '',
};

export function NotificationProvider({ children }) {
  const auth = useAuth();
  const { connected, subscribe, unsubscribe } = useRealtime();
  const [state, setState] = useState(initialState);
  const stateRef = useRef(state);
  const processedEventIdsRef = useRef(new Set());
  const realtimeNotificationIdsRef = useRef(new Set());
  const eligible = canUseNotificationRealtime(auth);

  useEffect(() => {
    stateRef.current = state;
  }, [state]);

  const reconcileUnreadCount = useCallback(async (signal) => {
    try {
      const response = await socialApi.getUnreadCount(signal);
      setState((current) => ({
        ...current,
        unreadCount: Math.max(0, Number(response.unreadCount) || 0),
        error: '',
      }));
    } catch (error) {
      if (!isRequestCanceled(error)) setState((current) => ({ ...current, error: error.message }));
    }
  }, []);

  const loadFirstPage = useCallback(async (signal, markLoading = true) => {
    if (markLoading) setState((current) => ({ ...current, loading: true }));
    try {
      const page = await socialApi.getNotifications(
        { page: 0, size: NOTIFICATION_FIRST_PAGE_SIZE },
        signal,
      );
      setState((current) => {
        const realtimeItems = current.notifications.filter((item) =>
          realtimeNotificationIdsRef.current.has(String(item.notificationId)));
        return {
          ...current,
          notifications: mergeUniqueNotifications(realtimeItems, page.content ?? []),
          initialized: true,
          page: page.page ?? 0,
          totalPages: page.totalPages ?? 0,
          last: Boolean(page.last),
          loading: false,
          error: '',
        };
      });
    } catch (error) {
      if (!isRequestCanceled(error)) {
        setState((current) => ({ ...current, loading: false, error: error.message }));
      }
    }
  }, []);

  const reconcile = useCallback(async () => {
    const tasks = [reconcileUnreadCount()];
    if (stateRef.current.initialized) tasks.push(loadFirstPage(undefined, false));
    await Promise.all(tasks);
  }, [loadFirstPage, reconcileUnreadCount]);

  const initializeNotifications = useCallback(async (signal) => {
    setState((current) => ({ ...current, loading: true }));
    await Promise.all([
      loadFirstPage(signal, false),
      reconcileUnreadCount(signal),
    ]);
    setState((current) => ({ ...current, loading: false }));
  }, [loadFirstPage, reconcileUnreadCount]);

  const loadMore = useCallback(async () => {
    const current = stateRef.current;
    if (!current.initialized || current.loadingMore || current.last) return;
    const nextPage = current.page + 1;
    setState((value) => ({ ...value, loadingMore: true }));
    try {
      const page = await socialApi.getNotifications({
        page: nextPage,
        size: NOTIFICATION_FIRST_PAGE_SIZE,
      });
      setState((value) => ({
        ...value,
        notifications: mergeUniqueNotifications(value.notifications, page.content ?? []),
        page: page.page ?? nextPage,
        totalPages: page.totalPages ?? value.totalPages,
        last: Boolean(page.last),
        loadingMore: false,
        error: '',
      }));
    } catch (error) {
      setState((value) => ({ ...value, loadingMore: false, error: error.message }));
    }
  }, []);

  const handleCreatedEvent = useCallback((event) => {
    if (!rememberEventId(processedEventIdsRef.current, event?.eventId)) return;
    if (event?.notificationId != null) {
      realtimeNotificationIdsRef.current.add(String(event.notificationId));
    }
    setState((current) => applyCreatedNotificationEvent(current, event));
  }, []);

  useEffect(() => {
    if (!eligible) {
      processedEventIdsRef.current.clear();
      realtimeNotificationIdsRef.current.clear();
      setState(initialState);
      return undefined;
    }

    reconcileUnreadCount();
    const subscriptionToken = subscribe(NOTIFICATION_DESTINATION, handleCreatedEvent);
    return () => unsubscribe(subscriptionToken);
  }, [
    eligible,
    handleCreatedEvent,
    reconcileUnreadCount,
    subscribe,
    unsubscribe,
  ]);

  useEffect(() => {
    setState((current) => ({ ...current, socketConnected: eligible && connected }));
    if (eligible && connected) reconcile();
  }, [connected, eligible, reconcile]);

  useEffect(() => {
    if (!eligible || state.socketConnected) return undefined;
    const poll = () => {
      if (shouldPollNotificationCount({
        eligible,
        socketConnected: stateRef.current.socketConnected,
        visibilityState: document.visibilityState,
      })) {
        reconcileUnreadCount();
      }
    };
    const intervalId = window.setInterval(poll, POLLING_INTERVAL_MS);
    return () => window.clearInterval(intervalId);
  }, [eligible, reconcileUnreadCount, state.socketConnected]);

  useEffect(() => {
    if (!eligible) return undefined;
    const handleVisibilityChange = () => {
      if (document.visibilityState === 'visible') reconcile();
    };
    document.addEventListener('visibilitychange', handleVisibilityChange);
    return () => document.removeEventListener('visibilitychange', handleVisibilityChange);
  }, [eligible, reconcile]);

  const markAsRead = useCallback(async (notificationId) => {
    try {
      const response = await socialApi.markNotificationRead(notificationId);
      setState((current) => markNotificationReadState(
        { ...current, error: '' },
        notificationId,
        response.readAt ?? new Date().toISOString(),
      ));
      return response;
    } catch (error) {
      setState((current) => ({ ...current, error: error.message }));
      throw error;
    }
  }, []);

  const markAllAsRead = useCallback(async () => {
    try {
      const response = await socialApi.markAllNotificationsRead();
      setState((current) => markAllNotificationsReadState(
        { ...current, error: '' },
        new Date().toISOString(),
      ));
      reconcileUnreadCount();
      return response;
    } catch (error) {
      setState((current) => ({ ...current, error: error.message }));
      throw error;
    }
  }, [reconcileUnreadCount]);

  const deleteNotification = useCallback(async (notificationId) => {
    try {
      const response = await socialApi.deleteNotification(notificationId);
      setState((current) => removeNotificationState(
        { ...current, error: '' },
        notificationId,
      ));
      return response;
    } catch (error) {
      setState((current) => ({ ...current, error: error.message }));
      throw error;
    }
  }, []);

  const value = useMemo(() => ({
    ...state,
    hasMore: state.initialized && !state.last,
    initializeNotifications,
    loadMore,
    reconcile,
    markAsRead,
    markAllAsRead,
    deleteNotification,
  }), [
    state,
    initializeNotifications,
    loadMore,
    reconcile,
    markAsRead,
    markAllAsRead,
    deleteNotification,
  ]);

  return <NotificationContext.Provider value={value}>{children}</NotificationContext.Provider>;
}

export function useNotifications() {
  const context = useContext(NotificationContext);
  if (!context) throw new Error('useNotifications phải được sử dụng bên trong NotificationProvider.');
  return context;
}
