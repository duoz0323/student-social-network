import { messagingTimestampMillis } from './messageTime.js';

export const INBOX_PAGE_SIZE = 20;
export const MESSAGE_PAGE_SIZE = 30;
const MAX_EVENT_IDS = 500;

export function messagingBadgeLabel(count) {
  const value = Math.max(0, Number(count) || 0);
  if (!value) return '';
  return value > 99 ? '99+' : String(value);
}

export function canUseMessaging(auth) {
  return Boolean(auth?.isAuthenticated && auth?.profileCompleted
    && auth?.role === 'USER' && auth?.authStatus !== 'BLOCKED');
}

export function rememberMessagingEventId(eventIds, eventId) {
  if (!eventId || eventIds.has(eventId)) return false;
  eventIds.add(eventId);
  while (eventIds.size > MAX_EVENT_IDS) eventIds.delete(eventIds.values().next().value);
  return true;
}

function messageKey(message) {
  return message?.messageId != null ? `id:${message.messageId}` : `client:${message?.clientMessageId}`;
}

/** Dedupe cả REST response, WebSocket echo và optimistic item bằng hai khóa ổn định. */
export function mergeMessages(existing = [], incoming = []) {
  const byMessageId = new Map();
  const byClientId = new Map();
  const merged = [];
  [...existing, ...incoming].forEach((candidate) => {
    if (!candidate) return;
    const messageId = candidate.messageId == null ? null : String(candidate.messageId);
    const clientId = candidate.clientMessageId || null;
    const index = (messageId && byMessageId.get(messageId)) ?? (clientId && byClientId.get(clientId));
    if (index != null) {
      merged[index] = { ...merged[index], ...candidate, deliveryState: candidate.deliveryState ?? 'SENT' };
    } else {
      const next = merged.push({ ...candidate }) - 1;
      if (messageId) byMessageId.set(messageId, next);
      if (clientId) byClientId.set(clientId, next);
    }
  });
  return merged.sort((left, right) => {
    if (left.messageId != null && right.messageId != null) return Number(left.messageId) - Number(right.messageId);
    return messagingTimestampMillis(left.createdAt) - messagingTimestampMillis(right.createdAt);
  });
}

export function prependHistory(current, older) {
  return mergeMessages(older, current);
}

export function moveConversationToFront(conversations, data, currentUserId) {
  const index = conversations.findIndex((item) => String(item.conversationId) === String(data.conversationId));
  if (index < 0) return conversations;
  const current = conversations[index];
  const updated = {
    ...current,
    lastMessage: {
      messageId: data.messageId,
      senderId: data.senderId,
      contentPreview: data.content,
      createdAt: data.createdAt,
    },
    unreadCount: String(data.senderId) === String(currentUserId)
      ? current.unreadCount
      : Math.max(0, Number(current.unreadCount) || 0) + 1,
  };
  return [updated, ...conversations.slice(0, index), ...conversations.slice(index + 1)];
}

export function advanceReadMarker(markers, readerId, messageId) {
  const key = String(readerId);
  const current = Number(markers[key]) || 0;
  if (Number(messageId) <= current) return markers;
  return { ...markers, [key]: Number(messageId) };
}

export function createOptimisticMessage({ conversationId, senderId, clientMessageId, content }) {
  return {
    messageId: null,
    conversationId: Number(conversationId),
    senderId,
    clientMessageId,
    type: 'TEXT',
    content,
    createdAt: new Date().toISOString(),
    deliveryState: 'SENDING',
    optimisticKey: messageKey({ clientMessageId }),
  };
}

export function shouldPollMessaging({ eligible, socketConnected, visibilityState }) {
  return Boolean(eligible && !socketConnected && visibilityState === 'visible');
}

export function preservedScrollTop({ previousHeight, nextHeight, previousTop }) {
  return Math.max(0, Number(nextHeight) - Number(previousHeight) + Number(previousTop));
}
