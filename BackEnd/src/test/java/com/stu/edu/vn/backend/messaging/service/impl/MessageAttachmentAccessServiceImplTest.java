package com.stu.edu.vn.backend.messaging.service.impl;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.stu.edu.vn.backend.common.exception.*;
import com.stu.edu.vn.backend.messaging.entity.*;
import com.stu.edu.vn.backend.messaging.repository.*;
import com.stu.edu.vn.backend.security.CurrentUserProvider;
import com.stu.edu.vn.backend.storage.*;
import com.stu.edu.vn.backend.user.entity.User;
import com.stu.edu.vn.backend.user.enums.*;
import com.stu.edu.vn.backend.user.repository.*;
import java.time.OffsetDateTime;
import java.util.Optional;
import org.junit.jupiter.api.*;
import org.springframework.test.util.ReflectionTestUtils;

/** Test chống IDOR và Block cho endpoint cấp URL ảnh chat. */
class MessageAttachmentAccessServiceImplTest {
    private final CurrentUserProvider current = mock(CurrentUserProvider.class);
    private final UserRepository users = mock(UserRepository.class);
    private final UserProfileRepository profiles = mock(UserProfileRepository.class);
    private final UserBlockRepository blocks = mock(UserBlockRepository.class);
    private final ConversationMemberRepository members = mock(ConversationMemberRepository.class);
    private final MessageAttachmentRepository attachments = mock(MessageAttachmentRepository.class);
    private final CloudinaryStorageService storage = mock(CloudinaryStorageService.class);
    private final MessageAttachmentAccessServiceImpl service = new MessageAttachmentAccessServiceImpl(
            current, users, profiles, blocks, members, attachments, storage);

    @BeforeEach
    void setUp() {
        when(current.getCurrentUserId()).thenReturn(10L);
        User viewer = user(10L);
        when(users.findById(10L)).thenReturn(Optional.of(viewer));
        when(profiles.existsByUserIdAndProfileCompletedAtIsNotNull(10L)).thenReturn(true);
    }

    @Test
    void validMemberGetsShortLivedAccessWithoutExposingStorageIdInMetadataContract() {
        MessageAttachment attachment = attachment();
        when(attachments.findForAccess(90L)).thenReturn(Optional.of(attachment));
        when(members.existsByIdConversationIdAndIdUserId(15L, 10L)).thenReturn(true);
        when(blocks.existsEitherDirection(10L, 20L)).thenReturn(false);
        OffsetDateTime expiresAt = OffsetDateTime.now().plusMinutes(5);
        when(storage.createMessageImageAccess("private-id", "image/png")).thenReturn(
                new CloudinaryAccessResult("https://signed.example/access", expiresAt));

        var response = service.createAccess(90L);
        assertThat(response.accessUrl()).contains("signed.example");
        assertThat(response.expiresAt()).isEqualTo(expiresAt);
    }

    @Test
    void outsiderCannotUseKnownAttachmentId() {
        when(attachments.findForAccess(90L)).thenReturn(Optional.of(attachment()));
        when(members.existsByIdConversationIdAndIdUserId(15L, 10L)).thenReturn(false);
        assertError(() -> service.createAccess(90L), ErrorCode.MESSAGE_ATTACHMENT_NOT_FOUND);
        verifyNoInteractions(storage);
    }

    @Test
    void blockEitherDirectionPreventsAccess() {
        when(attachments.findForAccess(90L)).thenReturn(Optional.of(attachment()));
        when(members.existsByIdConversationIdAndIdUserId(15L, 10L)).thenReturn(true);
        when(blocks.existsEitherDirection(10L, 20L)).thenReturn(true);
        assertError(() -> service.createAccess(90L), ErrorCode.DIRECT_MESSAGE_NOT_ALLOWED);
        verifyNoInteractions(storage);
    }

    private MessageAttachment attachment() {
        User low = user(10L);
        User high = user(20L);
        Conversation conversation = new Conversation(low, high);
        ReflectionTestUtils.setField(conversation, "id", 15L);
        Message message = new Message(conversation, low, "550e8400-e29b-41d4-a716-446655440000", "caption");
        ReflectionTestUtils.setField(message, "id", 80L);
        MessageAttachment attachment = new MessageAttachment(message, "private-id", "image/png", 100, 2, 3, 0);
        ReflectionTestUtils.setField(attachment, "id", 90L);
        return attachment;
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
