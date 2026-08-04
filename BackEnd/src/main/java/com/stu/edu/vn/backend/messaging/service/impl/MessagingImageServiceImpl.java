package com.stu.edu.vn.backend.messaging.service.impl;

import com.stu.edu.vn.backend.common.exception.*;
import com.stu.edu.vn.backend.follow.repository.FollowRepository;
import com.stu.edu.vn.backend.messaging.dto.request.SendImageMessageRequest;
import com.stu.edu.vn.backend.messaging.dto.response.*;
import com.stu.edu.vn.backend.messaging.entity.*;
import com.stu.edu.vn.backend.messaging.enums.MessageType;
import com.stu.edu.vn.backend.messaging.event.MessageCreatedEvent;
import com.stu.edu.vn.backend.messaging.repository.*;
import com.stu.edu.vn.backend.messaging.service.MessagingImageService;
import com.stu.edu.vn.backend.messaging.support.MessagePayloadFingerprint;
import com.stu.edu.vn.backend.messaging.validation.*;
import com.stu.edu.vn.backend.security.CurrentUserProvider;
import com.stu.edu.vn.backend.storage.*;
import com.stu.edu.vn.backend.storage.cleanup.MediaCleanupService;
import com.stu.edu.vn.backend.user.entity.User;
import com.stu.edu.vn.backend.user.enums.*;
import com.stu.edu.vn.backend.user.repository.*;
import com.stu.edu.vn.backend.user.service.UserPairLockCoordinator;
import jakarta.persistence.EntityManager;
import java.util.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

/** Upload ngoài transaction, sau đó commit message và attachment trong transaction MySQL ngắn. */
@Slf4j
@Service
@RequiredArgsConstructor
public class MessagingImageServiceImpl implements MessagingImageService {
    private final CurrentUserProvider currentUserProvider;
    private final UserRepository userRepository;
    private final UserProfileRepository profileRepository;
    private final UserBlockRepository blockRepository;
    private final FollowRepository followRepository;
    private final UserPairLockCoordinator pairLock;
    private final ConversationRepository conversationRepository;
    private final ConversationMemberRepository memberRepository;
    private final MessageRepository messageRepository;
    private final MessageAttachmentRepository attachmentRepository;
    private final MessageImageValidator imageValidator;
    private final CloudinaryStorageService storageService;
    private final MediaCleanupService cleanupService;
    private final TransactionTemplate transactionTemplate;
    private final EntityManager entityManager;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    public SendMessageResponse sendImageMessage(Long conversationId, SendImageMessageRequest request) {
        Long senderId = currentUserProvider.getCurrentUserId();
        String clientMessageId = requireUuidV4(request == null ? null : request.clientMessageId());
        List<ValidatedMessageImage> images = imageValidator.validate(request == null ? null : request.images());
        MessageType type = images.isEmpty() ? MessageType.TEXT : MessageType.IMAGE;
        String content = normalizeContent(request == null ? null : request.content(), type);
        String fingerprint = type == MessageType.TEXT
                ? MessagePayloadFingerprint.text(conversationId, content)
                : MessagePayloadFingerprint.image(conversationId, content, images);

        SendMessageResponse replay = transactionTemplate.execute(status ->
                preflight(conversationId, senderId, clientMessageId, type, content, fingerprint));
        if (replay != null) {
            return replay;
        }

        List<UploadedImage> uploaded = uploadAll(images);
        try {
            PersistResult result = transactionTemplate.execute(status ->
                    persist(conversationId, senderId, clientMessageId, type, content, fingerprint, uploaded));
            if (result == null) {
                throw new BusinessException(ErrorCode.INTERNAL_ERROR);
            }
            if (result.response().replayed()) {
                cleanup(uploaded, "IDEMPOTENCY_RACE");
            }
            return result.response();
        } catch (RuntimeException exception) {
            cleanup(uploaded, exception instanceof DataIntegrityViolationException
                    ? "IDEMPOTENCY_RACE" : "MESSAGE_TRANSACTION_ROLLBACK");
            SendMessageResponse raced = transactionTemplate.execute(status ->
                    findReplay(senderId, conversationId, clientMessageId, type, content, fingerprint, false));
            if (raced != null) {
                return raced;
            }
            throw exception;
        }
    }

    private SendMessageResponse preflight(Long conversationId, Long senderId, String clientMessageId,
                                          MessageType type, String content, String fingerprint) {
        requireEligible(senderId);
        Conversation conversation = requireMemberConversation(conversationId, senderId);
        Long otherUserId = conversation.otherParticipantId(senderId);
        requireEligible(otherUserId);
        requireNotBlocked(senderId, otherUserId);
        return findReplay(senderId, conversationId, clientMessageId, type, content, fingerprint, false);
    }

    private PersistResult persist(Long conversationId, Long senderId, String clientMessageId, MessageType type,
                                  String content, String fingerprint, List<UploadedImage> uploaded) {
        Conversation initial = requireMemberConversation(conversationId, senderId);
        Long otherUserId = initial.otherParticipantId(senderId);
        pairLock.lockPair(senderId, otherUserId);
        User sender = requireEligible(senderId);
        requireEligible(otherUserId);
        Conversation conversation = conversationRepository.findByIdForUpdate(conversationId)
                .filter(item -> memberRepository.existsByIdConversationIdAndIdUserId(item.getId(), senderId))
                .orElseThrow(() -> new BusinessException(ErrorCode.CONVERSATION_NOT_FOUND));
        requireNotBlocked(senderId, otherUserId);
        SendMessageResponse replay = findReplay(senderId, conversationId, clientMessageId, type, content, fingerprint, true);
        if (replay != null) {
            return new PersistResult(replay);
        }
        if (conversation.getLastMessage() == null
                && !followRepository.existsByIdFollowerIdAndIdFollowingId(otherUserId, senderId)) {
            throw new BusinessException(ErrorCode.DIRECT_MESSAGE_NOT_ALLOWED);
        }
        Message message = messageRepository.saveAndFlush(
                new Message(conversation, sender, clientMessageId, type, content, fingerprint));
        List<MessageAttachment> attachments = new ArrayList<>();
        for (int i = 0; i < uploaded.size(); i++) {
            UploadedImage item = uploaded.get(i);
            attachments.add(new MessageAttachment(message, item.result().publicId(), item.image().actualMimeType(),
                    item.image().fileSizeBytes(), item.image().width(), item.image().height(), i));
        }
        attachments = attachmentRepository.saveAllAndFlush(attachments);
        entityManager.refresh(message);
        conversation.setLastMessage(message);
        conversation.setLastMessageAt(message.getCreatedAt());
        conversationRepository.save(conversation);
        eventPublisher.publishEvent(new MessageCreatedEvent(message.getId(), conversationId));
        return new PersistResult(new SendMessageResponse(toResponse(message, attachments), false));
    }

    private SendMessageResponse findReplay(Long senderId, Long conversationId, String clientMessageId,
                                           MessageType type, String content, String fingerprint, boolean forUpdate) {
        Message old = forUpdate
                ? messageRepository.findBySenderAndClientMessageIdForUpdate(senderId, clientMessageId).orElse(null)
                : messageRepository.findBySenderIdAndClientMessageId(senderId, clientMessageId).orElse(null);
        if (old == null) {
            return null;
        }
        boolean same = old.getConversation().getId().equals(conversationId) && old.getType() == type
                && (type == MessageType.TEXT
                    ? Objects.equals(old.getContent(), content)
                    : Objects.equals(old.getPayloadFingerprint(), fingerprint));
        if (!same) {
            throw new BusinessException(ErrorCode.IDEMPOTENCY_KEY_REUSED);
        }
        List<MessageAttachment> attachments = attachmentRepository.findByMessageIdOrderByDisplayOrderAsc(old.getId());
        return new SendMessageResponse(toResponse(old, attachments), true);
    }

    private List<UploadedImage> uploadAll(List<ValidatedMessageImage> images) {
        List<UploadedImage> uploaded = new ArrayList<>();
        try {
            for (ValidatedMessageImage image : images) {
                uploaded.add(new UploadedImage(image, storageService.uploadMessageImage(image.file())));
            }
            return uploaded;
        } catch (RuntimeException exception) {
            cleanup(uploaded, "PARTIAL_UPLOAD");
            throw exception;
        }
    }

    private void cleanup(List<UploadedImage> uploaded, String reason) {
        for (UploadedImage item : uploaded) {
            try {
                storageService.deleteMessageImage(item.result().publicId());
            } catch (RuntimeException cleanupException) {
                try {
                    cleanupService.enqueue(item.result().publicId(), reason);
                } catch (RuntimeException enqueueException) {
                    log.error("Không thể ghi cleanup task cho media chat provider=CLOUDINARY reason={}", reason);
                }
            }
        }
    }

    private User requireEligible(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MESSAGING_NOT_ALLOWED));
        if (user.getRole() != UserRole.USER || user.getStatus() != UserStatus.ACTIVE
                || !profileRepository.existsByUserIdAndProfileCompletedAtIsNotNull(userId)) {
            throw new BusinessException(ErrorCode.MESSAGING_NOT_ALLOWED);
        }
        return user;
    }

    private Conversation requireMemberConversation(Long conversationId, Long userId) {
        if (conversationId == null) throw new BusinessException(ErrorCode.CONVERSATION_NOT_FOUND);
        return conversationRepository.findById(conversationId)
                .filter(item -> memberRepository.existsByIdConversationIdAndIdUserId(item.getId(), userId))
                .orElseThrow(() -> new BusinessException(ErrorCode.CONVERSATION_NOT_FOUND));
    }

    private void requireNotBlocked(Long first, Long second) {
        if (blockRepository.existsEitherDirection(first, second)) {
            throw new BusinessException(ErrorCode.DIRECT_MESSAGE_NOT_ALLOWED);
        }
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

    private String normalizeContent(String content, MessageType type) {
        if (content == null || content.trim().isEmpty()) {
            if (type == MessageType.TEXT) throw new BusinessException(ErrorCode.MESSAGE_CONTENT_REQUIRED);
            return null;
        }
        if (content.codePointCount(0, content.length()) > 2000) {
            throw new BusinessException(ErrorCode.MESSAGE_CONTENT_TOO_LONG);
        }
        return content;
    }

    private MessageResponse toResponse(Message message, List<MessageAttachment> attachments) {
        List<MessageAttachmentResponse> metadata = attachments.stream()
                .map(item -> new MessageAttachmentResponse(item.getId(), item.getMediaType(), item.getMimeType(),
                        item.getFileSizeBytes(), item.getWidth(), item.getHeight(), item.getDisplayOrder()))
                .toList();
        return new MessageResponse(message.getId(), message.getConversation().getId(), message.getSender().getId(),
                message.getClientMessageId(), message.getType(), message.getContent(), metadata, message.getCreatedAt());
    }

    private record UploadedImage(ValidatedMessageImage image, CloudinaryUploadResult result) {
    }
    private record PersistResult(SendMessageResponse response) {
    }
}
