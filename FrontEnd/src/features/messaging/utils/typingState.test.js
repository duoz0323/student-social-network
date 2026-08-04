import assert from 'node:assert/strict';
import test from 'node:test';
import {
  applyTypingEventState,
  createTypingComposerController,
  scheduleTypingExpiry,
  TYPING_EXPIRY_MS,
} from './typingState.js';

function fakeTimers() {
  let now = 0;
  let nextId = 1;
  const timers = new Map();
  return {
    setTimer(callback, delay) {
      const id = nextId++;
      timers.set(id, { callback, due: now + delay });
      return id;
    },
    clearTimer(id) { timers.delete(id); },
    advance(milliseconds) {
      now += milliseconds;
      let due;
      do {
        due = [...timers.entries()].filter(([, timer]) => timer.due <= now)
          .sort((left, right) => left[1].due - right[1].due)[0];
        if (due) {
          timers.delete(due[0]);
          due[1].callback();
        }
      } while (due);
    },
    count() { return timers.size; },
  };
}

function fixture() {
  const timers = fakeTimers();
  const frames = [];
  let connected = true;
  const controller = createTypingComposerController({
    isConnected: () => connected,
    sendFrame: (conversationId, typing) => { frames.push({ conversationId, typing }); return true; },
    setTimer: timers.setTimer,
    clearTimer: timers.clearTimer,
  });
  return { controller, frames, timers, disconnect: () => { connected = false; controller.disconnected(); } };
}

test('lần gõ đầu gửi START nhưng mỗi phím tiếp theo không gửi frame liên tục', () => {
  const { controller, frames, timers } = fixture();
  controller.update(15, 'a');
  controller.update(15, 'ab');
  assert.deepEqual(frames, [{ conversationId: 15, typing: true }]);
  assert.equal(timers.count(), 2);
});

test('idle 2 giây, xóa input, submit/blur đều gửi STOP đúng một lần', () => {
  const idle = fixture();
  idle.controller.update(15, 'a');
  idle.timers.advance(2_000);
  assert.deepEqual(idle.frames.map((frame) => frame.typing), [true, false]);

  const blank = fixture();
  blank.controller.update(15, 'a');
  blank.controller.update(15, '   ');
  blank.controller.stop();
  assert.deepEqual(blank.frames.map((frame) => frame.typing), [true, false]);
});

test('đang gõ liên tục gia hạn START tối đa mỗi 3 giây và cleanup khi rời conversation', () => {
  const { controller, frames, timers } = fixture();
  controller.update(15, 'a');
  timers.advance(1_500);
  controller.update(15, 'ab');
  timers.advance(1_500);
  assert.deepEqual(frames.map((frame) => frame.typing), [true, true]);
  controller.dispose();
  assert.deepEqual(frames.map((frame) => frame.typing), [true, true, false]);
  assert.equal(timers.count(), 0);
});

test('disconnected không SEND và reconnect không tự khôi phục typing cũ', () => {
  const state = fixture();
  state.controller.update(15, 'a');
  state.disconnect();
  state.timers.advance(10_000);
  assert.deepEqual(state.frames.map((frame) => frame.typing), [true]);
  assert.equal(state.controller.isActive(), false);
});

test('receiver chỉ nhận typing của participant khác, STOP xóa state và expiry là 5 giây', () => {
  const start = { eventType: 'TYPING_STARTED', data: { conversationId: 15, userId: 20 } };
  const stopped = { eventType: 'TYPING_STOPPED', data: start.data };
  const typing = applyTypingEventState({}, start, 10);
  assert.equal(typing['15:20'], true);
  assert.equal(applyTypingEventState(typing, stopped, 10)['15:20'], undefined);
  const ownTypingState = {};
  assert.equal(applyTypingEventState(
    ownTypingState, { ...start, data: { ...start.data, userId: 10 } }, 10,
  ), ownTypingState);

  let scheduledDelay;
  let expired = false;
  scheduleTypingExpiry(() => { expired = true; }, (callback, delay) => { scheduledDelay = delay; callback(); return 1; });
  assert.equal(scheduledDelay, TYPING_EXPIRY_MS);
  assert.equal(expired, true);
});
