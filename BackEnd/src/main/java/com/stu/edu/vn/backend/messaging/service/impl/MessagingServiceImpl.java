package com.stu.edu.vn.backend.messaging.service.impl;

import com.stu.edu.vn.backend.common.api.CursorPageResponse;
import com.stu.edu.vn.backend.common.cursor.CursorCodec;
import com.stu.edu.vn.backend.common.exception.BusinessException;
import com.stu.edu.vn.backend.common.exception.ErrorCode;
import com.stu.edu.vn.backend.follow.repository.FollowRepository;
import com.stu.edu.vn.backend.messaging.cursor.*;
import com.stu.edu.vn.backend.messaging.dto.request.*;
import com.stu.edu.vn.backend.messaging.dto.response.*;
import com.stu.edu.vn.backend.messaging.enums.MessageType;
import com.stu.edu.vn.backend.messaging.entity.*;
import com.stu.edu.vn.backend.messaging.event.MessageCreatedEvent;
import com.stu.edu.vn.backend.messaging.event.MessagesReadEvent;
import com.stu.edu.vn.backend.messaging.mapper.MessagingMapper;
import com.stu.edu.vn.backend.messaging.projection.ConversationListProjection;
import com.stu.edu.vn.backend.messaging.repository.*;
import com.stu.edu.vn.backend.messaging.service.MessagingService;
import com.stu.edu.vn.backend.security.CurrentUserProvider;
import com.stu.edu.vn.backend.user.entity.*;
import com.stu.edu.vn.backend.user.enums.*;
import com.stu.edu.vn.backend.user.repository.*;
import com.stu.edu.vn.backend.user.service.UserPairLockCoordinator;
import jakarta.persistence.EntityManager;
import java.time.*;
import java.util.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.transaction.annotation.Transactional;

/** Điều phối authorization, Block/Follow, keyset, idempotency và marker đọc trong transaction. */
@Service
@RequiredArgsConstructor
public class MessagingServiceImpl implements MessagingService {
    private final CurrentUserProvider currentUserProvider;
    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final UserBlockRepository userBlockRepository;
    private final FollowRepository followRepository;
    private final UserPairLockCoordinator userPairLockCoordinator;
    private final ConversationRepository conversationRepository;
    private final ConversationMemberRepository memberRepository;
    private final MessageRepository messageRepository;
    private final MessageAttachmentRepository attachmentRepository;
    private final MessagingMapper messagingMapper;
    private final CursorCodec cursorCodec;
    private final EntityManager entityManager;
    private final Clock clock;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional(readOnly = true)
    public CursorPageResponse<ConversationResponse> getConversations(int limit, String encodedCursor) {
        Long userId = requireCurrentMessagingUser().getId();
        requireLimit(limit, 50);
        ConversationCursor cursor = decodeConversationCursor(encodedCursor);
        List<ConversationListProjection> rows = cursor == null
                ? conversationRepository.findInboxFirstPage(userId, limit + 1)
                : conversationRepository.findInboxAfter(userId, cursor.lastMessageAt(), cursor.conversationId(), limit + 1);
        boolean hasNext = rows.size() > limit;
        List<ConversationListProjection> page = rows.subList(0, Math.min(limit, rows.size()));
        String nextCursor = hasNext && !page.isEmpty()
                ? cursorCodec.encode(new ConversationCursor(page.getLast().getLastMessageAt(), page.getLast().getConversationId()))
                : null;
        return new CursorPageResponse<>(page.stream().map(messagingMapper::toConversationResponse).toList(),
                nextCursor, hasNext);
    }

    @Override
    @Transactional
    public DirectConversationResponse openDirectConversation(Long recipientUserId) {
        User sender = requireCurrentMessagingUser();
        if (recipientUserId == null || sender.getId().equals(recipientUserId)) {
            throw new BusinessException(ErrorCode.CANNOT_MESSAGE_SELF);
        }
        userPairLockCoordinator.lockPair(sender.getId(), recipientUserId);
        sender = requireMessagingUser(sender.getId());
        User recipient = requireMessagingUser(recipientUserId);
        requireNotBlocked(sender.getId(), recipientUserId);
        long lowId = Math.min(sender.getId(), recipientUserId);
        long highId = Math.max(sender.getId(), recipientUserId);
        Optional<Conversation> existing = conversationRepository.findPairForUpdate(lowId, highId);
        Conversation conversation;
        if (existing.isPresent()) {
            conversation = existing.get();
        } else {
            // Bắt đầu A -> B chỉ khi B đang Follow A, đúng hướng follower=B/following=A.
            if (!followRepository.existsByIdFollowerIdAndIdFollowingId(recipientUserId, sender.getId())) {
                throw new BusinessException(ErrorCode.DIRECT_MESSAGE_NOT_ALLOWED);
            }
            User low = sender.getId().equals(lowId) ? sender : recipient;
            User high = sender.getId().equals(highId) ? sender : recipient;
            conversation = conversationRepository.saveAndFlush(new Conversation(low, high));
            memberRepository.saveAll(List.of(new ConversationMember(conversation, low),
                    new ConversationMember(conversation, high)));
            memberRepository.flush();
        }
        UserProfile profile = userProfileRepository.findById(recipientUserId)
                .orElseThrow(() -> new BusinessException(ErrorCode.DIRECT_MESSAGE_NOT_ALLOWED));
        return new DirectConversationResponse(conversation.getId(),
                new MessagingUserResponse(recipientUserId, profile.getDisplayName(), profile.getAvatarUrl()),
                conversation.getLastMessage() != null);
    }

    @Override
    @Transactional(readOnly = true)
    public CursorPageResponse<MessageResponse> getMessages(Long conversationId, int limit, String encodedCursor) {
        Long userId = requireCurrentMessagingUser().getId();
        requireLimit(limit, 100);
        Conversation conversation = requireAccessibleConversation(conversationId, userId);
        MessageCursor cursor = decodeMessageCursor(encodedCursor);
        List<Message> rows = cursor == null
                ? messageRepository.findFirstPage(conversation.getId(), limit + 1)
                : messageRepository.findAfter(conversation.getId(), cursor.messageId(), limit + 1);
        boolean hasNext = rows.size() > limit;
        List<Message> pageDescending = new ArrayList<>(rows.subList(0, Math.min(limit, rows.size())));
        String nextCursor = hasNext && !pageDescending.isEmpty()
                ? cursorCodec.encode(new MessageCursor(pageDescending.getLast().getId())) : null;
        Collections.reverse(pageDescending);
        List<MessageAttachment> attachmentRows = attachmentRepository
                .findByMessageIdInOrderByMessageIdAscDisplayOrderAsc(
                        pageDescending.stream().map(Message::getId).toList());
        Map<Long, List<MessageAttachment>> attachmentsByMessage = attachmentRows.stream()
                .collect(java.util.stream.Collectors.groupingBy(item -> item.getMessage().getId()));
        List<MessageResponse> responses = pageDescending.stream()
                .map(message -> messagingMapper.toMessageResponse(
                        message, attachmentsByMessage.getOrDefault(message.getId(), List.of())))
                .toList();
        return new CursorPageResponse<>(responses, nextCursor, hasNext);
    }

    @Override
    @Transactional
    public SendMessageResponse sendMessage(Long conversationId, SendMessageRequest request) {
        User sender = requireCurrentMessagingUser();
        Conversation initial = requireMemberConversation(conversationId, sender.getId());
        Long otherUserId = initial.otherParticipantId(sender.getId());
        userPairLockCoordinator.lockPair(sender.getId(), otherUserId);
        sender = requireMessagingUser(sender.getId());
        requireMessagingUser(otherUserId);
        Long senderId = sender.getId();
        Conversation conversation = conversationRepository.findByIdForUpdate(conversationId)
                .filter(item -> memberRepository.existsByIdConversationIdAndIdUserId(item.getId(), senderId))
                .orElseThrow(() -> new BusinessException(ErrorCode.CONVERSATION_NOT_FOUND));
        requireNotBlocked(senderId, otherUserId);
        String clientMessageId = requireUuidV4(request == null ? null : request.clientMessageId());
        String content = requireContent(request == null ? null : request.content());
        Optional<Message> replay = messageRepository.findBySenderAndClientMessageIdForUpdate(senderId, clientMessageId);
        if (replay.isPresent()) {
            Message old = replay.get();
            if (old.getType() != MessageType.TEXT || !old.getConversation().getId().equals(conversationId)
                    || !old.getContent().equals(content)) {
                throw new BusinessException(ErrorCode.IDEMPOTENCY_KEY_REUSED);
            }
            return new SendMessageResponse(messagingMapper.toMessageResponse(old), true);
        }
        if (conversation.getLastMessage() == null
                && !followRepository.existsByIdFollowerIdAndIdFollowingId(otherUserId, sender.getId())) {
            throw new BusinessException(ErrorCode.DIRECT_MESSAGE_NOT_ALLOWED);
        }
        Message message = messageRepository.saveAndFlush(new Message(conversation, sender, clientMessageId, content));
        entityManager.refresh(message);
        conversation.setLastMessage(message);
        conversation.setLastMessageAt(message.getCreatedAt());
        conversationRepository.save(conversation);
        // Event nhẹ được ghi nhận trong transaction; listener chỉ chạy khi transaction commit thành công.
        eventPublisher.publishEvent(new MessageCreatedEvent(message.getId(), conversation.getId()));
        return new SendMessageResponse(messagingMapper.toMessageResponse(message), false);
    }

    @Override
    @Transactional
    public MarkConversationReadResponse markRead(Long conversationId, MarkConversationReadRequest request) {
        User user = requireCurrentMessagingUser();
        Conversation initial = requireMemberConversation(conversationId, user.getId());
        Long otherUserId = initial.otherParticipantId(user.getId());
        userPairLockCoordinator.lockPair(user.getId(), otherUserId);
        requireNotBlocked(user.getId(), otherUserId);
        ConversationMember member = memberRepository.findForUpdate(conversationId, user.getId())
                .orElseThrow(() -> new BusinessException(ErrorCode.CONVERSATION_NOT_FOUND));
        Long requestedId = request == null ? null : request.lastReadMessageId();
        Message requested = requestedId == null ? null : messageRepository.findById(requestedId).orElse(null);
        if (requested == null || !requested.getConversation().getId().equals(conversationId)) {
            throw new BusinessException(ErrorCode.MESSAGE_NOT_FOUND);
        }
        Long currentId = member.getLastReadMessage() == null ? null : member.getLastReadMessage().getId();
        boolean updated = currentId == null || requestedId > currentId;
        if (updated) {
            member.advanceReadMarker(requested, LocalDateTime.now(clock));
            memberRepository.saveAndFlush(member);
            eventPublisher.publishEvent(new MessagesReadEvent(conversationId, user.getId(), requestedId));
        }
        return new MarkConversationReadResponse(conversationId,
                updated ? requestedId : currentId, member.getLastReadAt(), updated,
                messageRepository.countUnread(user.getId()));
    }

    @Override
    @Transactional(readOnly = true)
    public MessagingUnreadCountResponse getUnreadCount() {
        Long userId = requireCurrentMessagingUser().getId();
        return new MessagingUnreadCountResponse(messageRepository.countUnread(userId));
    }

    private User requireCurrentMessagingUser() {
        return requireMessagingUser(currentUserProvider.getCurrentUserId());
    }

    private User requireMessagingUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MESSAGING_NOT_ALLOWED));
        if (user.getRole() != UserRole.USER || user.getStatus() != UserStatus.ACTIVE
                || !userProfileRepository.existsByUserIdAndProfileCompletedAtIsNotNull(userId)) {
            throw new BusinessException(ErrorCode.MESSAGING_NOT_ALLOWED);
        }
        return user;
    }

    private Conversation requireAccessibleConversation(Long conversationId, Long userId) {
        Conversation conversation = requireMemberConversation(conversationId, userId);
        requireNotBlocked(userId, conversation.otherParticipantId(userId));
        return conversation;
    }

    private Conversation requireMemberConversation(Long conversationId, Long userId) {
        if (conversationId == null) throw new BusinessException(ErrorCode.CONVERSATION_NOT_FOUND);
        return conversationRepository.findById(conversationId)
                .filter(item -> memberRepository.existsByIdConversationIdAndIdUserId(item.getId(), userId))
                .orElseThrow(() -> new BusinessException(ErrorCode.CONVERSATION_NOT_FOUND));
    }

    private void requireNotBlocked(Long firstUserId, Long secondUserId) {
        if (userBlockRepository.existsEitherDirection(firstUserId, secondUserId)) {
            throw new BusinessException(ErrorCode.DIRECT_MESSAGE_NOT_ALLOWED);
        }
    }

    private void requireLimit(int limit, int max) {
        if (limit < 1 || limit > max) throw new BusinessException(ErrorCode.VALIDATION_ERROR);
    }

    private ConversationCursor decodeConversationCursor(String encoded) {
        ConversationCursor cursor = cursorCodec.decode(encoded, ConversationCursor.class);
        if (cursor != null && (cursor.lastMessageAt() == null || cursor.conversationId() == null
                || cursor.conversationId() <= 0)) throw new BusinessException(ErrorCode.INVALID_CURSOR);
        return cursor;
    }

    private MessageCursor decodeMessageCursor(String encoded) {
        MessageCursor cursor = cursorCodec.decode(encoded, MessageCursor.class);
        if (cursor != null && (cursor.messageId() == null || cursor.messageId() <= 0)) {
            throw new BusinessException(ErrorCode.INVALID_CURSOR);
        }
        return cursor;
    }

    private String requireUuidV4(String value) {
        try {
            UUID uuid = UUID.fromString(value);
            if (uuid.version() != 4 || !uuid.toString().equalsIgnoreCase(value)) throw new IllegalArgumentException();
            return uuid.toString();
        } catch (RuntimeException exception) {
            throw new BusinessException(ErrorCode.INVALID_CLIENT_MESSAGE_ID);
        }
    }

    private String requireContent(String content) {
        if (content == null || content.trim().isEmpty()) {
            throw new BusinessException(ErrorCode.MESSAGE_CONTENT_REQUIRED);
        }
        if (content.codePointCount(0, content.length()) > 2000) {
            throw new BusinessException(ErrorCode.MESSAGE_CONTENT_TOO_LONG);
        }
        return content;
    }
}
