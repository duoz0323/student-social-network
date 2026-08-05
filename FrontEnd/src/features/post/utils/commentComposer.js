/**
 * Enter gửi nội dung; Shift+Enter vẫn xuống dòng và IME không bị submit giữa lúc ghép ký tự.
 */
export function shouldSubmitComposerOnEnter(event) {
  return event?.key === 'Enter'
    && !event.shiftKey
    && !event.isComposing
    && !event.nativeEvent?.isComposing;
}
