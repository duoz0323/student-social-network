package com.stu.edu.vn.backend.messaging.service.impl;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.stu.edu.vn.backend.common.exception.*;
import com.stu.edu.vn.backend.follow.repository.FollowRepository;
import com.stu.edu.vn.backend.messaging.dto.request.SendImageMessageRequest;
import com.stu.edu.vn.backend.messaging.entity.*;
import com.stu.edu.vn.backend.messaging.enums.MessageType;
import com.stu.edu.vn.backend.messaging.event.MessageCreatedEvent;
import com.stu.edu.vn.backend.messaging.repository.*;
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
import java.time.LocalDateTime;
import java.util.*;
import org.junit.jupiter.api.*;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.*;

/** Test orchestration upload ngoài transaction, replay và compensation. */
class MessagingImageServiceImplTest {
    private final CurrentUserProvider current = mock(CurrentUserProvider.class);
    private final UserRepository users = mock(UserRepository.class);
    private final UserProfileRepository profiles = mock(UserProfileRepository.class);
    private final UserBlockRepository blocks = mock(UserBlockRepository.class);
    private final FollowRepository follows = mock(FollowRepository.class);
    private final UserPairLockCoordinator pairLock = mock(UserPairLockCoordinator.class);
    private final ConversationRepository conversations = mock(ConversationRepository.class);
    private final ConversationMemberRepository members = mock(ConversationMemberRepository.class);
    private final MessageRepository messages = mock(MessageRepository.class);
    private final MessageAttachmentRepository attachments = mock(MessageAttachmentRepository.class);
    private final MessageImageValidator validator = mock(MessageImageValidator.class);
    private final CloudinaryStorageService storage = mock(CloudinaryStorageService.class);
    private final MediaCleanupService cleanup = mock(MediaCleanupService.class);
    private final TransactionTemplate transactions = mock(TransactionTemplate.class);
    private final EntityManager entityManager = mock(EntityManager.class);
    private final ApplicationEventPublisher events = mock(ApplicationEventPublisher.class);
    private MessagingImageServiceImpl service;
    private Conversation conversation;
    private User sender;
    private ValidatedMessageImage image;

    @BeforeEach
    void setUp() {
        service = new MessagingImageServiceImpl(current, users, profiles, blocks, follows, pairLock,
                conversations, members, messages, attachments, validator, storage, cleanup,
                transactions, entityManager, events);
        when(transactions.execute(any())).thenAnswer(invocation -> {
            TransactionCallback<?> callback = invocation.getArgument(0);
            return callback.doInTransaction(mock(TransactionStatus.class));
        });
        when(current.getCurrentUserId()).thenReturn(10L);
        sender = user(10L);
        User recipient = user(20L);
        conversation = new Conversation(sender, recipient);
        ReflectionTestUtils.setField(conversation, "id", 15L);
        when(users.findById(10L)).thenReturn(Optional.of(sender));
        when(users.findById(20L)).thenReturn(Optional.of(recipient));
        when(profiles.existsByUserIdAndProfileCompletedAtIsNotNull(anyLong())).thenReturn(true);
        when(messages.findBySenderAndClientMessageIdForUpdate(anyLong(), anyString())).thenReturn(Optional.empty());
        when(conversations.findById(15L)).thenReturn(Optional.of(conversation));
        when(conversations.findByIdForUpdate(15L)).thenReturn(Optional.of(conversation));
        when(members.existsByIdConversationIdAndIdUserId(15L, 10L)).thenReturn(true);
        when(blocks.existsEitherDirection(10L, 20L)).thenReturn(false);
        when(follows.existsByIdFollowerIdAndIdFollowingId(20L, 10L)).thenReturn(true);
        image = new ValidatedMessageImage(
                new MockMultipartFile("images", "a.png", "image/png", new byte[]{1, 2, 3}),
                "image/png", 3, 2, 3, "a".repeat(64));
    }

    @Test
    void exactReplayReturnsOldMessageWithoutUploadingOrPublishingAgain() {
        when(validator.validate(any())).thenReturn(List.of(image));
        String fingerprint = MessagePayloadFingerprint.image(15L, "caption", List.of(image));
        Message old = message(MessageType.IMAGE, "caption", fingerprint);
        when(messages.findBySenderIdAndClientMessageId(10L, uuid())).thenReturn(Optional.of(old));
        when(attachments.findByMessageIdOrderByDisplayOrderAsc(81L)).thenReturn(List.of());

        assertThat(service.sendImageMessage(15L, request("caption", List.of(image.file()))).replayed()).isTrue();
        verifyNoInteractions(storage);
        verify(events, never()).publishEvent(any(MessageCreatedEvent.class));
    }

    @Test
    void reusedKeyWithDifferentImageFingerprintIsRejectedBeforeUpload() {
        when(validator.validate(any())).thenReturn(List.of(image));
        when(messages.findBySenderIdAndClientMessageId(10L, uuid()))
                .thenReturn(Optional.of(message(MessageType.IMAGE, null, "different")));
        assertError(() -> service.sendImageMessage(15L, request(null, List.of(image.file()))),
                ErrorCode.IDEMPOTENCY_KEY_REUSED);
        verifyNoInteractions(storage);
    }

    @Test
    void emptyMultipartAndCaptionOverTwoThousandCodePointsAreRejected() {
        when(validator.validate(any())).thenReturn(List.of());
        assertError(() -> service.sendImageMessage(15L, request(null, List.of())),
                ErrorCode.MESSAGE_CONTENT_REQUIRED);
        when(validator.validate(any())).thenReturn(List.of(image));
        assertError(() -> service.sendImageMessage(15L, request("😀".repeat(2001), List.of(image.file()))),
                ErrorCode.MESSAGE_CONTENT_TOO_LONG);
        verifyNoInteractions(storage);
    }

    @Test
    void databaseRollbackDeletesUploadedFileAndDoesNotPublishRealtime() {
        when(validator.validate(any())).thenReturn(List.of(image));
        when(messages.findBySenderIdAndClientMessageId(10L, uuid())).thenReturn(Optional.empty());
        when(storage.uploadMessageImage(any())).thenReturn(upload("asset-1"));
        when(messages.saveAndFlush(any())).thenThrow(new IllegalStateException("database down"));

        assertThatThrownBy(() -> service.sendImageMessage(15L, request(null, List.of(image.file()))))
                .isInstanceOf(IllegalStateException.class);
        verify(storage).deleteMessageImage("asset-1");
        verify(events, never()).publishEvent(any(MessageCreatedEvent.class));
    }

    @Test
    void directCleanupFailureCreatesDurableTaskWithoutMaskingDatabaseError() {
        when(validator.validate(any())).thenReturn(List.of(image));
        when(messages.findBySenderIdAndClientMessageId(10L, uuid())).thenReturn(Optional.empty());
        when(storage.uploadMessageImage(any())).thenReturn(upload("asset-2"));
        when(messages.saveAndFlush(any())).thenThrow(new IllegalStateException("database down"));
        doThrow(new IllegalStateException("delete down")).when(storage).deleteMessageImage("asset-2");

        assertThatThrownBy(() -> service.sendImageMessage(15L, request(null, List.of(image.file()))))
                .isInstanceOf(IllegalStateException.class).hasMessage("database down");
        verify(cleanup).enqueue("asset-2", "MESSAGE_TRANSACTION_ROLLBACK");
    }

    @Test
    void oneImageWithCaptionPersistsMetadataAndPublishesCreatedEvent() {
        when(validator.validate(any())).thenReturn(List.of(image));
        prepareSuccessfulPersistence();
        var response = service.sendImageMessage(15L, request("caption", List.of(image.file())));
        assertThat(response.replayed()).isFalse();
        assertThat(response.message().type()).isEqualTo(MessageType.IMAGE);
        assertThat(response.message().content()).isEqualTo("caption");
        assertThat(response.message().attachments()).hasSize(1);
        assertThat(response.message().attachments().getFirst().mimeType()).isEqualTo("image/png");
        verify(events).publishEvent(any(MessageCreatedEvent.class));
    }

    @Test
    void fiveImagesAreUploadedOnceAndKeepDisplayOrder() {
        List<ValidatedMessageImage> five = List.of(image, image, image, image, image);
        when(validator.validate(any())).thenReturn(five);
        prepareSuccessfulPersistence();
        var response = service.sendImageMessage(15L,
                request(null, five.stream().map(ValidatedMessageImage::file).toList()));
        assertThat(response.message().attachments()).hasSize(5)
                .extracting(item -> item.displayOrder().intValue()).containsExactly(0, 1, 2, 3, 4);
        verify(storage, times(5)).uploadMessageImage(any());
    }
    @Test
    void outsiderAndBlockedMemberAreRejectedBeforeUpload() {
        when(validator.validate(any())).thenReturn(List.of(image));
        when(members.existsByIdConversationIdAndIdUserId(15L, 10L)).thenReturn(false);
        assertError(() -> service.sendImageMessage(15L, request(null, List.of(image.file()))),
                ErrorCode.CONVERSATION_NOT_FOUND);
        verifyNoInteractions(storage);

        clearInvocations(storage);
        when(members.existsByIdConversationIdAndIdUserId(15L, 10L)).thenReturn(true);
        when(blocks.existsEitherDirection(10L, 20L)).thenReturn(true);
        assertError(() -> service.sendImageMessage(15L, request(null, List.of(image.file()))),
                ErrorCode.DIRECT_MESSAGE_NOT_ALLOWED);
        verifyNoInteractions(storage);
    }


    private void prepareSuccessfulPersistence() {
        when(messages.findBySenderIdAndClientMessageId(10L, uuid())).thenReturn(Optional.empty());
        java.util.concurrent.atomic.AtomicInteger sequence = new java.util.concurrent.atomic.AtomicInteger();
        when(storage.uploadMessageImage(any())).thenAnswer(invocation -> upload("asset-" + sequence.incrementAndGet()));
        when(messages.saveAndFlush(any())).thenAnswer(invocation -> {
            Message saved = invocation.getArgument(0);
            ReflectionTestUtils.setField(saved, "id", 100L);
            ReflectionTestUtils.setField(saved, "createdAt", LocalDateTime.of(2026, 8, 3, 10, 0));
            return saved;
        });
        when(attachments.saveAllAndFlush(anyList())).thenAnswer(invocation -> {
            List<MessageAttachment> saved = invocation.getArgument(0);
            for (int i = 0; i < saved.size(); i++) {
                ReflectionTestUtils.setField(saved.get(i), "id", 200L + i);
            }
            return saved;
        });
    }

    private Message message(MessageType type, String content, String fingerprint) {
        Message message = new Message(conversation, sender, uuid(), type, content, fingerprint);
        ReflectionTestUtils.setField(message, "id", 81L);
        ReflectionTestUtils.setField(message, "createdAt", LocalDateTime.of(2026, 8, 3, 10, 0));
        return message;
    }

    private CloudinaryUploadResult upload(String publicId) {
        return new CloudinaryUploadResult(null, publicId, "image/png", 3L, 2, 3);
    }

    private SendImageMessageRequest request(String content, List<org.springframework.web.multipart.MultipartFile> files) {
        return new SendImageMessageRequest(uuid(), content, files);
    }

    private String uuid() {
        return "550e8400-e29b-41d4-a716-446655440000";
    }

    private User user(Long id) {
        User user = new User("u" + id + "@example.com", "hash");
        ReflectionTestUtils.setField(user, "id", id);
        ReflectionTestUtils.setField(user, "status", UserStatus.ACTIVE);
        return user;
    }

    private void assertError(org.assertj.core.api.ThrowableAssert.ThrowingCallable action, ErrorCode code) {
        assertThatThrownBy(action).isInstanceOf(BusinessException.class)
                .extracting(error -> ((BusinessException) error).getErrorCode()).isEqualTo(code);
    }
}
