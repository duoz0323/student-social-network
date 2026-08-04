/* eslint-disable react-refresh/only-export-components */
import { createContext, useCallback, useContext, useEffect, useMemo, useRef, useState } from 'react';
import { isRequestCanceled } from '../api/apiError.js';
import { useAuth } from '../features/auth/hooks/useAuth.js';
import { messagingApi } from '../features/messaging/services/messagingApi.js';
import {
  advanceReadMarker,
  canUseMessaging,
  createOptimisticMessage,
  INBOX_PAGE_SIZE,
  mergeMessages,
  MESSAGE_PAGE_SIZE,
  moveConversationToFront,
  prependHistory,
  rememberMessagingEventId,
  shouldPollMessaging,
} from '../features/messaging/utils/messagingState.js';
import { useRealtime } from './RealtimeContext.jsx';
import { applyTypingEventState, scheduleTypingExpiry, typingKey } from '../features/messaging/utils/typingState.js';
import { createImageMessageFormData } from '../features/messaging/utils/messageImages.js';

const DESTINATION = '/user/queue/messaging';
const POLLING_INTERVAL_MS = 30_000;
const MessagingContext = createContext(null);
const initialState = {
  conversations: [], totalUnreadCount: 0, activeConversationId: null, messages: [],
  readMarkers: {}, inboxCursor: null, messageCursor: null, hasMoreConversations: false,
  hasMoreMessages: false, loadingInbox: false, loadingMessages: false, loadingMoreInbox: false,
  loadingMoreMessages: false, initialized: false, socketConnected: false, error: '',
  accessRevokedConversationId: null,
  typingUsers: {},
};

function isAccessRevoked(error) {
  return [403, 404].includes(error?.status)
    || ['DIRECT_MESSAGE_NOT_ALLOWED', 'CONVERSATION_NOT_FOUND', 'MESSAGING_NOT_ALLOWED'].includes(error?.code);
}

export function MessagingProvider({ children }) {
  const auth = useAuth();
  const currentUserId = auth.user?.id ?? null;
  const { connected, send: sendRealtime, subscribe, unsubscribe } = useRealtime();
  const [state, setState] = useState(initialState);
  const stateRef = useRef(state);
  const eventIdsRef = useRef(new Set());
  const lastMarkedRef = useRef(new Map());
  const typingTimersRef = useRef(new Map());
  const eligible = canUseMessaging(auth);

  useEffect(() => { stateRef.current = state; }, [state]);

  const clearTypingConversation = useCallback((conversationId) => {
    const prefix = `${conversationId}:`;
    typingTimersRef.current.forEach((timerId, key) => {
      if (key.startsWith(prefix)) {
        window.clearTimeout(timerId);
        typingTimersRef.current.delete(key);
      }
    });
    setState((current) => ({
      ...current,
      typingUsers: Object.fromEntries(
        Object.entries(current.typingUsers).filter(([key]) => !key.startsWith(prefix)),
      ),
    }));
  }, []);

  const clearAllTyping = useCallback((updateState = true) => {
    typingTimersRef.current.forEach((timerId) => window.clearTimeout(timerId));
    typingTimersRef.current.clear();
    if (updateState) setState((current) => ({ ...current, typingUsers: {} }));
  }, []);

  const revokeConversation = useCallback((conversationId) => {
    clearTypingConversation(conversationId);
    setState((current) => ({
      ...current,
      conversations: current.conversations.filter((item) => String(item.conversationId) !== String(conversationId)),
      activeConversationId: String(current.activeConversationId) === String(conversationId) ? null : current.activeConversationId,
      messages: String(current.activeConversationId) === String(conversationId) ? [] : current.messages,
      accessRevokedConversationId: conversationId,
    }));
  }, [clearTypingConversation]);

  const loadUnreadCount = useCallback(async (signal) => {
    const response = await messagingApi.getUnreadCount(signal);
    setState((current) => ({ ...current, totalUnreadCount: Math.max(0, Number(response.unreadCount) || 0) }));
  }, []);

  const loadInbox = useCallback(async ({ cursor = null, append = false, signal } = {}) => {
    setState((current) => ({ ...current, [append ? 'loadingMoreInbox' : 'loadingInbox']: true }));
    try {
      const page = await messagingApi.getConversations({ limit: INBOX_PAGE_SIZE, cursor }, signal);
      setState((current) => ({
        ...current,
        conversations: append ? [...current.conversations, ...(page.content ?? [])]
          .filter((item, index, list) => list.findIndex((value) => String(value.conversationId) === String(item.conversationId)) === index)
          : (page.content ?? []),
        inboxCursor: page.nextCursor ?? null,
        hasMoreConversations: Boolean(page.hasNext),
        loadingInbox: false, loadingMoreInbox: false, initialized: true, error: '',
      }));
    } catch (error) {
      if (!isRequestCanceled(error)) setState((current) => ({ ...current, loadingInbox: false, loadingMoreInbox: false, error: error.message }));
    }
  }, []);

  const loadConversation = useCallback(async (conversationId, { reconcile = false } = {}) => {
    if (!conversationId) return;
    if (!reconcile) {
      const previousId = stateRef.current.activeConversationId;
      if (previousId && String(previousId) !== String(conversationId)) clearTypingConversation(previousId);
      setState((current) => ({ ...current, activeConversationId: Number(conversationId), messages: [], loadingMessages: true, messageCursor: null, hasMoreMessages: false, error: '', accessRevokedConversationId: null }));
    }
    try {
      const page = await messagingApi.getMessages(conversationId, { limit: MESSAGE_PAGE_SIZE });
      setState((current) => {
        if (String(current.activeConversationId) !== String(conversationId)) return current;
        return { ...current, messages: reconcile ? mergeMessages(current.messages, page.content ?? []) : mergeMessages([], page.content ?? []), messageCursor: page.nextCursor ?? null, hasMoreMessages: Boolean(page.hasNext), loadingMessages: false, error: '' };
      });
    } catch (error) {
      if (isAccessRevoked(error)) revokeConversation(conversationId);
      else if (!isRequestCanceled(error)) setState((current) => ({ ...current, loadingMessages: false, error: error.message }));
    }
  }, [clearTypingConversation, revokeConversation]);

  const loadOlderMessages = useCallback(async () => {
    const current = stateRef.current;
    if (!current.activeConversationId || !current.hasMoreMessages || current.loadingMoreMessages || !current.messageCursor) return false;
    setState((value) => ({ ...value, loadingMoreMessages: true }));
    try {
      const page = await messagingApi.getMessages(current.activeConversationId, { limit: MESSAGE_PAGE_SIZE, cursor: current.messageCursor });
      setState((value) => ({ ...value, messages: prependHistory(value.messages, page.content ?? []), messageCursor: page.nextCursor ?? null, hasMoreMessages: Boolean(page.hasNext), loadingMoreMessages: false, error: '' }));
      return true;
    } catch (error) {
      if (isAccessRevoked(error)) revokeConversation(current.activeConversationId);
      else setState((value) => ({ ...value, loadingMoreMessages: false, error: error.message }));
      return false;
    }
  }, [revokeConversation]);

  const reconcile = useCallback(async () => {
    if (!eligible) return;
    const activeId = stateRef.current.activeConversationId;
    await Promise.allSettled([loadUnreadCount(), loadInbox(), activeId ? loadConversation(activeId, { reconcile: true }) : Promise.resolve()]);
  }, [eligible, loadConversation, loadInbox, loadUnreadCount]);

  const handleEvent = useCallback((event) => {
    if (!rememberMessagingEventId(eventIdsRef.current, event?.eventId)) return;
    const data = event?.data;
    if (!data) return;
    if (event.eventType === 'TYPING_STARTED' || event.eventType === 'TYPING_STOPPED') {
      if (String(data.userId) === String(currentUserId)) return;
      const key = typingKey(data.conversationId, data.userId);
      const currentTimer = typingTimersRef.current.get(key);
      if (currentTimer != null) window.clearTimeout(currentTimer);
      if (event.eventType === 'TYPING_STARTED') {
        const timerId = scheduleTypingExpiry(() => {
          typingTimersRef.current.delete(key);
          setState((current) => {
            const nextTyping = { ...current.typingUsers };
            delete nextTyping[key];
            return { ...current, typingUsers: nextTyping };
          });
        }, window.setTimeout);
        typingTimersRef.current.set(key, timerId);
        setState((current) => ({ ...current, typingUsers: applyTypingEventState(current.typingUsers, event, currentUserId) }));
      } else {
        typingTimersRef.current.delete(key);
        setState((current) => ({ ...current, typingUsers: applyTypingEventState(current.typingUsers, event, currentUserId) }));
      }
      return;
    }
    setState((current) => {
      if (event.eventType === 'MESSAGE_CREATED') {
        const isActive = String(current.activeConversationId) === String(data.conversationId);
        return { ...current, conversations: moveConversationToFront(current.conversations, data, currentUserId), messages: isActive ? mergeMessages(current.messages, [{ ...data, deliveryState: 'SENT' }]) : current.messages, totalUnreadCount: Math.max(0, Number(event.unreadCount) || 0) };
      }
      if (event.eventType === 'MESSAGES_READ') {
        const markers = advanceReadMarker(current.readMarkers, data.readerId, data.lastReadMessageId);
        if (markers === current.readMarkers) return current;
        const readerIsCurrent = String(data.readerId) === String(currentUserId);
        return { ...current, readMarkers: markers, totalUnreadCount: readerIsCurrent ? Math.max(0, Number(event.unreadCount) || 0) : current.totalUnreadCount, conversations: readerIsCurrent ? current.conversations.map((item) => String(item.conversationId) === String(data.conversationId) && Number(item.lastMessage?.messageId) <= Number(data.lastReadMessageId) ? { ...item, unreadCount: 0 } : item) : current.conversations };
      }
      return current;
    });
    if (event.eventType === 'MESSAGE_CREATED'
      && !stateRef.current.conversations.some((item) => String(item.conversationId) === String(data.conversationId))) loadInbox();
  }, [currentUserId, loadInbox]);

  useEffect(() => {
    if (!eligible) {
      eventIdsRef.current.clear(); lastMarkedRef.current.clear(); setState(initialState); return undefined;
    }
    loadUnreadCount(); loadInbox();
    const token = subscribe(DESTINATION, handleEvent);
    return () => {
      unsubscribe(token);
      clearAllTyping();
    };
  }, [clearAllTyping, eligible, handleEvent, loadInbox, loadUnreadCount, subscribe, unsubscribe]);

  useEffect(() => {
    setState((current) => ({ ...current, socketConnected: eligible && connected }));
    if (!connected) clearAllTyping();
    if (eligible && connected) reconcile();
  }, [clearAllTyping, connected, eligible, reconcile]);

  useEffect(() => () => clearAllTyping(false), [clearAllTyping]);

  useEffect(() => {
    if (!eligible || connected) return undefined;
    const id = window.setInterval(() => {
      if (shouldPollMessaging({ eligible, socketConnected: stateRef.current.socketConnected, visibilityState: document.visibilityState })) reconcile();
    }, POLLING_INTERVAL_MS);
    return () => window.clearInterval(id);
  }, [connected, eligible, reconcile]);

  useEffect(() => {
    if (!eligible) return undefined;
    const onVisible = () => { if (document.visibilityState === 'visible') reconcile(); };
    document.addEventListener('visibilitychange', onVisible);
    return () => document.removeEventListener('visibilitychange', onVisible);
  }, [eligible, reconcile]);

  const sendMessage = useCallback(async (conversationId, content, failedMessage = null) => {
    const clientMessageId = failedMessage?.clientMessageId ?? crypto.randomUUID();
    const optimistic = failedMessage ? { ...failedMessage, deliveryState: 'SENDING' }
      : createOptimisticMessage({ conversationId, senderId: currentUserId, clientMessageId, content });
    setState((current) => ({ ...current, messages: mergeMessages(current.messages, [optimistic]), error: '' }));
    try {
      const response = await messagingApi.sendMessage(conversationId, { clientMessageId, content });
      setState((current) => ({ ...current, messages: mergeMessages(current.messages, [{ ...response.message, deliveryState: 'SENT' }]) }));
      return response;
    } catch (error) {
      if (isAccessRevoked(error)) revokeConversation(conversationId);
      else setState((current) => ({ ...current, messages: current.messages.map((item) => item.clientMessageId === clientMessageId ? { ...item, deliveryState: 'FAILED', errorCode: error.code } : item), error: error.message }));
      throw error;
    }
  }, [currentUserId, revokeConversation]);

  const sendImageMessage = useCallback(async (conversationId, content, images) => {
    const clientMessageId = crypto.randomUUID();
    const formData = createImageMessageFormData({ clientMessageId, content, images });
    try {
      const response = await messagingApi.sendImageMessage(conversationId, formData);
      setState((current) => ({
        ...current,
        messages: mergeMessages(current.messages, [{ ...response.message, deliveryState: 'SENT' }]),
        error: '',
      }));
      return response;
    } catch (error) {
      if (isAccessRevoked(error)) revokeConversation(conversationId);
      else setState((current) => ({ ...current, error: error.message }));
      throw error;
    }
  }, [revokeConversation]);

  const markRead = useCallback(async (conversationId, messageId) => {
    const previous = Number(lastMarkedRef.current.get(String(conversationId))) || 0;
    if (!messageId || Number(messageId) <= previous) return null;
    lastMarkedRef.current.set(String(conversationId), Number(messageId));
    try {
      const response = await messagingApi.markRead(conversationId, messageId);
      setState((current) => ({ ...current, totalUnreadCount: Number(response.totalUnreadCount) || 0, readMarkers: advanceReadMarker(current.readMarkers, currentUserId, response.lastReadMessageId), conversations: current.conversations.map((item) => String(item.conversationId) === String(conversationId) && Number(item.lastMessage?.messageId) <= Number(response.lastReadMessageId) ? { ...item, unreadCount: 0 } : item) }));
      return response;
    } catch (error) {
      lastMarkedRef.current.delete(String(conversationId));
      if (isAccessRevoked(error)) revokeConversation(conversationId);
      throw error;
    }
  }, [currentUserId, revokeConversation]);

  const sendTyping = useCallback((conversationId, typing) => {
    if (!connected || !conversationId) return false;
    return sendRealtime('/app/messaging/typing', { conversationId: Number(conversationId), typing: Boolean(typing) });
  }, [connected, sendRealtime]);

  const value = useMemo(() => ({ ...state, currentUserId, reconcile, loadInbox, loadMoreConversations: () => loadInbox({ cursor: stateRef.current.inboxCursor, append: true }), loadConversation, loadOlderMessages, sendMessage, sendImageMessage, sendTyping, markRead, clearTypingConversation, clearAccessRevoked: () => setState((current) => ({ ...current, accessRevokedConversationId: null })) }), [state, currentUserId, reconcile, loadInbox, loadConversation, loadOlderMessages, sendMessage, sendImageMessage, sendTyping, markRead, clearTypingConversation]);
  return <MessagingContext.Provider value={value}>{children}</MessagingContext.Provider>;
}

export function useMessaging() {
  const context = useContext(MessagingContext);
  if (!context) throw new Error('useMessaging phải được dùng bên trong MessagingProvider.');
  return context;
}
