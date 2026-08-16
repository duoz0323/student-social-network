import { Fragment, useEffect, useState } from 'react';
import Avatar from '../../../components/common/Avatar.jsx';
import MessageAttachmentGrid from './MessageAttachmentGrid.jsx';
import SharedPostMessage from './SharedPostMessage.jsx';
import { formatMessageGroupTimestamp, messagingTimestampMillis, millisecondsUntilNextMessagingDay } from '../utils/messageTime.js';

const TIME_GROUP_GAP_MS = 15 * 60 * 1000;

function formatTime(value, referenceTime) {
  return value ? formatMessageGroupTimestamp(value, referenceTime) : '';
}

/** Threads chỉ hiện thời gian khi bắt đầu luồng hoặc hai tin cách nhau đủ lâu. */
function shouldShowTime(messages, index) {
  if (index === 0) return true;
  const currentTime = messagingTimestampMillis(messages[index]?.createdAt);
  const previousTime = messagingTimestampMillis(messages[index - 1]?.createdAt);
  return !Number.isFinite(currentTime) || !Number.isFinite(previousTime)
    || currentTime - previousTime >= TIME_GROUP_GAP_MS;
}

function MessageAvatar({ user }) {
  return <Avatar src={user?.avatarUrl} name={user?.displayName} size="sm" viewable className="!h-8 !w-8 text-xs" />;
}

/** Nội dung được render như text React, không dùng HTML động để tránh stored XSS. */
export default function MessageThread({ messages, currentUserId, otherReadMarker, otherUser, onRetry }) {
  const [referenceTime, setReferenceTime] = useState(() => new Date());
  const lastSeenMineIndex = messages.findLastIndex((message) => String(message.senderId) === String(currentUserId)
    && message.messageId && Number(otherReadMarker) >= Number(message.messageId));

  useEffect(() => {
    // Cập nhật đúng lúc sang ngày mới để nhãn giờ tự chuyển thành “Hôm qua”.
    const timerId = window.setTimeout(
      () => setReferenceTime(new Date()),
      millisecondsUntilNextMessagingDay(referenceTime) + 250,
    );
    return () => window.clearTimeout(timerId);
  }, [referenceTime]);

  return messages.map((message, index) => {
    const mine = String(message.senderId) === String(currentUserId);
    const showTime = shouldShowTime(messages, index);
    const nextMessage = messages[index + 1];
    const nextContinuesGroup = nextMessage
      && String(nextMessage.senderId) === String(message.senderId)
      && !shouldShowTime(messages, index + 1);
    const showAvatar = !mine && !nextContinuesGroup;
    const showSeen = mine && index === lastSeenMineIndex;
    const showDeliveryState = ['SENDING', 'FAILED'].includes(message.deliveryState);

    return (
      <Fragment key={message.messageId ?? message.clientMessageId}>
        {showTime ? <time className="my-5 text-center text-xs text-[var(--app-muted)]">{formatTime(message.createdAt, referenceTime)}</time> : null}
        <div className={`flex items-end gap-2 ${mine ? 'justify-end' : 'justify-start'}`}>
          {!mine ? <span className="w-8 shrink-0">{showAvatar ? <MessageAvatar user={otherUser} /> : null}</span> : null}
          <div className="max-w-[82%] text-sm sm:max-w-[68%]">
            <MessageAttachmentGrid attachments={message.attachments} />
            {message.content ? <p className={`whitespace-pre-wrap break-words rounded-3xl bg-[#262628] px-4 py-2.5 text-white ${message.attachments?.length || message.type === 'POST_SHARE' ? 'mb-1.5' : ''}`}>{message.content}</p> : null}
            {message.type === 'POST_SHARE' ? <SharedPostMessage post={message.sharedPost} unavailable={message.sharedPostUnavailable} /> : null}
            {showSeen || showDeliveryState ? (
              <div className="mt-1 flex items-center justify-end gap-2 px-1 text-[10px] text-[var(--app-muted)]">
                {showSeen ? <span>Đã xem</span> : null}
                {message.deliveryState === 'SENDING' ? <span>Đang gửi</span> : null}
                {message.deliveryState === 'FAILED' ? <button type="button" className="font-bold underline" onClick={() => onRetry(message)}>Gửi lại</button> : null}
              </div>
            ) : null}
          </div>
        </div>
      </Fragment>
    );
  });
}
