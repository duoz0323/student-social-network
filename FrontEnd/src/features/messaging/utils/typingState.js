export const TYPING_IDLE_MS = 2_000;
export const TYPING_REFRESH_MS = 3_000;
export const TYPING_EXPIRY_MS = 5_000;

export function typingKey(conversationId, userId) {
  return `${conversationId}:${userId}`;
}

export function applyTypingEventState(typingUsers, event, currentUserId) {
  const data = event?.data;
  if (!data || String(data.userId) === String(currentUserId)) return typingUsers;
  const key = typingKey(data.conversationId, data.userId);
  const next = { ...typingUsers };
  if (event.eventType === 'TYPING_STARTED') next[key] = true;
  else if (event.eventType === 'TYPING_STOPPED') delete next[key];
  return next;
}

export function scheduleTypingExpiry(onExpire, setTimer = setTimeout) {
  return setTimer(onExpire, TYPING_EXPIRY_MS);
}

/** Điều phối frame composer để thao tác gõ phím không phát SEND liên tục. */
export function createTypingComposerController({
  sendFrame,
  isConnected,
  setTimer = setTimeout,
  clearTimer = clearTimeout,
}) {
  let conversationId = null;
  let active = false;
  let idleTimer = null;
  let refreshTimer = null;

  const clearTimers = () => {
    if (idleTimer != null) clearTimer(idleTimer);
    if (refreshTimer != null) clearTimer(refreshTimer);
    idleTimer = null;
    refreshTimer = null;
  };

  const emit = (typing) => {
    if (!conversationId || !isConnected()) return false;
    return sendFrame(conversationId, typing);
  };

  const scheduleRefresh = () => {
    refreshTimer = setTimer(() => {
      refreshTimer = null;
      if (!active) return;
      emit(true);
      scheduleRefresh();
    }, TYPING_REFRESH_MS);
  };

  const scheduleIdle = () => {
    if (idleTimer != null) clearTimer(idleTimer);
    idleTimer = setTimer(() => {
      idleTimer = null;
      if (active) controller.stop();
    }, TYPING_IDLE_MS);
  };

  const controller = {
    update(nextConversationId, content) {
      if (String(conversationId) !== String(nextConversationId)) {
        controller.stop();
        conversationId = nextConversationId;
      }
      if (!String(content ?? '').trim().length) {
        controller.stop();
        return;
      }
      if (!active) {
        active = true;
        emit(true);
        scheduleRefresh();
      }
      scheduleIdle();
    },

    stop() {
      if (active) emit(false);
      active = false;
      clearTimers();
    },

    disconnected() {
      // Không gửi STOP khi socket đã mất và không tự khôi phục typing cũ sau reconnect.
      active = false;
      clearTimers();
    },

    dispose() {
      controller.stop();
      conversationId = null;
    },

    isActive() { return active; },
  };
  return controller;
}
