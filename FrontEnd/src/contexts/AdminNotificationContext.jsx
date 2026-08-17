/* eslint-disable react-refresh/only-export-components */
import { createContext, useCallback, useContext, useEffect, useMemo, useRef, useState } from 'react';
import { adminApi } from '../api/adminApi.js';
import { useAuth } from '../features/auth/hooks/useAuth.js';
import { useRealtime } from './RealtimeContext.jsx';
import { applyAdminNotificationEvent, mergeAdminNotifications } from '../features/admin/notifications/adminNotificationState.js';

const DESTINATION = '/user/queue/admin-notifications';
const CHANNEL_NAME = 'unishare-admin-notifications';
const initialState = { items: [], unreadCount: 0, nextCursor: null, hasNext: false, loading: false, loadingMore: false, error: '', initialized: false };
const AdminNotificationContext = createContext(null);

export function AdminNotificationProvider({ children }) {
  const auth = useAuth();
  const { connected, subscribe, unsubscribe } = useRealtime();
  const [state, setState] = useState(initialState);
  const stateRef = useRef(state);
  const processedEvents = useRef(new Set());
  const channelRef = useRef(null);
  const eligible = auth.isAuthenticated && auth.profileCompleted && auth.role === 'ADMIN' && auth.authStatus !== 'BLOCKED';

  useEffect(() => { stateRef.current = state; }, [state]);

  const loadUnreadCount = useCallback(async () => {
    try {
      const result = await adminApi.getNotificationUnreadCount();
      setState((current) => ({ ...current, unreadCount: Math.max(0, Number(result.unreadCount) || 0), error: '' }));
    } catch (error) {
      setState((current) => ({ ...current, error: error.message }));
    }
  }, []);

  const loadFirstPage = useCallback(async (showLoading = true) => {
    if (showLoading) setState((current) => ({ ...current, loading: true }));
    try {
      const page = await adminApi.getNotifications({ limit: 10 });
      setState((current) => ({
        ...current,
        // REST là nguồn đối soát cuối cùng để loại bỏ item không còn thấy sau khi quyền bị thu hồi.
        items: mergeAdminNotifications([], page.content ?? []),
        nextCursor: page.nextCursor,
        hasNext: Boolean(page.hasNext),
        initialized: true,
        loading: false,
        error: '',
      }));
    } catch (error) {
      setState((current) => ({ ...current, loading: false, error: error.message }));
    }
  }, []);

  const reconcile = useCallback(async () => {
    await Promise.all([loadUnreadCount(), loadFirstPage(false)]);
  }, [loadFirstPage, loadUnreadCount]);

  const loadMore = useCallback(async () => {
    const current = stateRef.current;
    if (!current.hasNext || current.loadingMore || !current.nextCursor) return;
    setState((value) => ({ ...value, loadingMore: true }));
    try {
      const page = await adminApi.getNotifications({ limit: 10, cursor: current.nextCursor });
      setState((value) => ({ ...value, items: mergeAdminNotifications(value.items, page.content ?? []), nextCursor: page.nextCursor, hasNext: Boolean(page.hasNext), loadingMore: false, error: '' }));
    } catch (error) {
      setState((value) => ({ ...value, loadingMore: false, error: error.message }));
    }
  }, []);

  const broadcast = useCallback((type) => channelRef.current?.postMessage({ type }), []);

  const markRead = useCallback(async (id) => {
    const result = await adminApi.markNotificationRead(id);
    setState((current) => ({ ...current, items: current.items.map((item) => String(item.notificationId) === String(id) ? { ...item, readAt: result.readAt ?? new Date().toISOString() } : item), unreadCount: Math.max(0, current.unreadCount - (current.items.some((item) => String(item.notificationId) === String(id) && !item.readAt) ? 1 : 0)) }));
    broadcast('RECONCILE');
    return result;
  }, [broadcast]);

  const markAllRead = useCallback(async () => {
    await adminApi.markAllNotificationsRead();
    // Đọc lại từ server vì thao tác chỉ áp dụng trên tập thông báo còn được phép xem tại thời điểm hiện tại.
    await reconcile();
    broadcast('RECONCILE');
  }, [broadcast, reconcile]);

  const deleteNotification = useCallback(async (id) => {
    await adminApi.deleteNotification(id);
    setState((current) => ({ ...current, items: current.items.filter((item) => String(item.notificationId) !== String(id)) }));
    await loadUnreadCount();
    broadcast('RECONCILE');
  }, [broadcast, loadUnreadCount]);

  useEffect(() => {
    if (!eligible) { setState(initialState); return undefined; }
    loadUnreadCount();
    const token = subscribe(DESTINATION, (envelope) => {
      if (!envelope?.eventId || processedEvents.current.has(envelope.eventId)) return;
      processedEvents.current.add(envelope.eventId);
      if (processedEvents.current.size > 200) processedEvents.current.delete(processedEvents.current.values().next().value);
      setState((current) => applyAdminNotificationEvent(current, envelope));
    });
    return () => unsubscribe(token);
  }, [eligible, loadUnreadCount, subscribe, unsubscribe]);

  useEffect(() => { if (eligible && connected) reconcile(); }, [connected, eligible, reconcile]);
  useEffect(() => {
    if (!eligible) return undefined;
    const onVisible = () => { if (document.visibilityState === 'visible') reconcile(); };
    document.addEventListener('visibilitychange', onVisible);
    return () => document.removeEventListener('visibilitychange', onVisible);
  }, [eligible, reconcile]);
  useEffect(() => {
    if (!eligible || typeof BroadcastChannel === 'undefined') return undefined;
    const channel = new BroadcastChannel(CHANNEL_NAME);
    channelRef.current = channel;
    channel.onmessage = (event) => { if (event.data?.type === 'RECONCILE') reconcile(); };
    return () => { channel.close(); channelRef.current = null; };
  }, [eligible, reconcile]);

  const value = useMemo(() => ({ ...state, connected, loadFirstPage, loadMore, reconcile, markRead, markAllRead, deleteNotification }), [state, connected, loadFirstPage, loadMore, reconcile, markRead, markAllRead, deleteNotification]);
  return <AdminNotificationContext.Provider value={value}>{children}</AdminNotificationContext.Provider>;
}

export function useAdminNotifications() {
  const context = useContext(AdminNotificationContext);
  if (!context) throw new Error('useAdminNotifications phải nằm trong AdminNotificationProvider.');
  return context;
}
