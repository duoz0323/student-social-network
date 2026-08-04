import test from 'node:test';
import assert from 'node:assert/strict';
import {
  advanceReadMarker,
  createOptimisticMessage,
  mergeMessages,
  messagingBadgeLabel,
  moveConversationToFront,
  prependHistory,
  preservedScrollTop,
  rememberMessagingEventId,
  shouldPollMessaging,
} from './messagingState.js';

test('REST response và WebSocket echo thay optimistic theo clientMessageId, không tạo duplicate', () => {
  const optimistic = createOptimisticMessage({ conversationId: 15, senderId: 10, clientMessageId: 'same-key', content: 'hello' });
  const real = { messageId: 901, conversationId: 15, senderId: 10, clientMessageId: 'same-key', content: 'hello', createdAt: '2026-08-03T10:00:00' };
  const merged = mergeMessages([optimistic], [real, real]);
  assert.equal(merged.length, 1);
  assert.equal(merged[0].messageId, 901);
  assert.equal(merged[0].deliveryState, 'SENT');
});

test('retry giữ nguyên clientMessageId của failed item', () => {
  const failed = { ...createOptimisticMessage({ conversationId: 15, senderId: 10, clientMessageId: 'retry-key', content: 'hello' }), deliveryState: 'FAILED' };
  const retry = { ...failed, deliveryState: 'SENDING' };
  assert.equal(retry.clientMessageId, 'retry-key');
  assert.equal(mergeMessages([failed], [retry])[0].deliveryState, 'SENDING');
});

test('message mới đưa conversation có sẵn lên đầu và không nhân đôi', () => {
  const conversations = [{ conversationId: 1 }, { conversationId: 15, unreadCount: 2 }, { conversationId: 3 }];
  const result = moveConversationToFront(conversations, { conversationId: 15, messageId: 901, senderId: 20, content: 'new', createdAt: '2026-08-03T10:00:00' }, 10);
  assert.deepEqual(result.map((item) => item.conversationId), [15, 1, 3]);
  assert.equal(result[0].unreadCount, 3);
});

test('marker đọc chỉ tiến lên', () => {
  const first = advanceReadMarker({}, 20, 901);
  assert.equal(advanceReadMarker(first, 20, 900), first);
  assert.equal(advanceReadMarker(first, 20, 902)['20'], 902);
});

test('prepend history giữ thứ tự và loại message trùng', () => {
  const result = prependHistory([{ messageId: 3 }, { messageId: 4 }], [{ messageId: 1 }, { messageId: 2 }, { messageId: 3 }]);
  assert.deepEqual(result.map((item) => item.messageId), [1, 2, 3, 4]);
  assert.equal(preservedScrollTop({ previousHeight: 600, nextHeight: 900, previousTop: 20 }), 320);
});

test('badge tách biệt hiển thị 99+ và eventId được dedupe', () => {
  assert.equal(messagingBadgeLabel(0), '');
  assert.equal(messagingBadgeLabel(100), '99+');
  const ids = new Set();
  assert.equal(rememberMessagingEventId(ids, 'event-1'), true);
  assert.equal(rememberMessagingEventId(ids, 'event-1'), false);
});

test('polling chỉ chạy khi eligible, disconnected và tab visible', () => {
  assert.equal(shouldPollMessaging({ eligible: true, socketConnected: false, visibilityState: 'visible' }), true);
  assert.equal(shouldPollMessaging({ eligible: true, socketConnected: true, visibilityState: 'visible' }), false);
  assert.equal(shouldPollMessaging({ eligible: true, socketConnected: false, visibilityState: 'hidden' }), false);
});
