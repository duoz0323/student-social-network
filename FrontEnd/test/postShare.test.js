import assert from 'node:assert/strict';
import { readFile } from 'node:fs/promises';
import test from 'node:test';
import {
  canonicalPostUrl,
  FACEBOOK_SHARE_STATUS,
  openFacebookPostShare,
} from '../src/features/post/utils/postViewModel.js';
import { mergeMessages, moveConversationToFront } from '../src/features/messaging/utils/messagingState.js';

test('canonical Post URL và Facebook sharer dùng đúng route Post Detail', () => {
  assert.equal(canonicalPostUrl(125, 'https://unishare.example'), 'https://unishare.example/posts/125');
  let popup;
  const popupHandle = { opener: {} };
  assert.equal(openFacebookPostShare(
    125,
    (...args) => { popup = args; return popupHandle; },
    undefined,
    'https://unishare.example',
  ), FACEBOOK_SHARE_STATUS.OPENED);
  assert.equal(popup[1], 'facebook-share');
  const target = new URL(popup[0]);
  assert.equal(target.origin, 'https://www.facebook.com');
  assert.equal(target.searchParams.get('u'), 'https://unishare.example/posts/125');
  assert.equal(popupHandle.opener, null);
  let sdkPayload;
  assert.equal(openFacebookPostShare(
    126,
    undefined,
    { ui: (payload) => { sdkPayload = payload; } },
    'https://unishare.example',
  ), FACEBOOK_SHARE_STATUS.OPENED);
  assert.deepEqual(sdkPayload, { method: 'share', href: 'https://unishare.example/posts/126' });
});

test('Facebook share từ chối URL local và phân biệt popup bị chặn', () => {
  let opened = false;
  assert.equal(openFacebookPostShare(
    125,
    () => { opened = true; return {}; },
    undefined,
    'http://localhost:5173',
  ), FACEBOOK_SHARE_STATUS.PUBLIC_URL_REQUIRED);
  assert.equal(opened, false);
  assert.equal(openFacebookPostShare(
    125,
    () => null,
    undefined,
    'https://unishare.example',
  ), FACEBOOK_SHARE_STATUS.POPUP_BLOCKED);
});

test('POST_SHARE merge giữ preview viewer-specific và cập nhật Inbox type', () => {
  const message = {
    messageId: 9, conversationId: 3, senderId: 2, clientMessageId: 'client',
    type: 'POST_SHARE', content: null, sharedPost: { postId: 125 }, sharedPostUnavailable: false,
    createdAt: '2026-08-15T10:00:00Z',
  };
  assert.deepEqual(mergeMessages([], [message])[0].sharedPost, { postId: 125 });
  const conversations = [{ conversationId: 3, unreadCount: 0, lastMessage: {} }];
  const next = moveConversationToFront(conversations, message, 1);
  assert.equal(next[0].lastMessage.type, 'POST_SHARE');
  assert.equal(next[0].unreadCount, 1);
});

test('PostCard mở Share Modal và Messaging render unavailable state', async () => {
  const [postCard, dialog, thread, sharedPost] = await Promise.all([
    readFile(new URL('../src/features/post/components/PostCard.jsx', import.meta.url), 'utf8'),
    readFile(new URL('../src/features/post/components/SharePostDialog.jsx', import.meta.url), 'utf8'),
    readFile(new URL('../src/features/messaging/components/MessageThread.jsx', import.meta.url), 'utf8'),
    readFile(new URL('../src/features/messaging/components/SharedPostMessage.jsx', import.meta.url), 'utf8'),
  ]);
  assert.match(postCard, /setSharing\(true\)/);
  assert.match(postCard, /<SharePostDialog/);
  assert.doesNotMatch(postCard, /className="post-action" onClick=\{handleCopyPostLink\}/);
  assert.match(dialog, /getShareRecipients/);
  assert.match(dialog, /sendPostShare/);
  assert.match(dialog, /sharedPostId: Number\(postId\)/);
  assert.match(dialog, /Sao chép liên kết/);
  assert.match(dialog, /openFacebookPostShare/);
  assert.match(thread, /message\.type === 'POST_SHARE'/);
  assert.match(sharedPost, /Bài viết không còn khả dụng/);
  assert.match(sharedPost, /navigate\(`\/posts\/\$\{postId\}`\)/);
});
