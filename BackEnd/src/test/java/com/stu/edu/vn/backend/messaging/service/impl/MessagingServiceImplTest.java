package com.stu.edu.vn.backend.messaging.service.impl;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.stu.edu.vn.backend.common.cursor.CursorCodec;
import com.stu.edu.vn.backend.common.exception.*;
import com.stu.edu.vn.backend.follow.repository.FollowRepository;
import com.stu.edu.vn.backend.messaging.dto.request.SendMessageRequest;
import com.stu.edu.vn.backend.messaging.dto.request.MarkConversationReadRequest;
import com.stu.edu.vn.backend.messaging.cursor.MessageCursor;
import com.stu.edu.vn.backend.messaging.entity.*;
import com.stu.edu.vn.backend.messaging.event.MessageCreatedEvent;
import com.stu.edu.vn.backend.messaging.event.MessagesReadEvent;
import com.stu.edu.vn.backend.messaging.mapper.MessagingMapper;
import com.stu.edu.vn.backend.messaging.repository.*;
import com.stu.edu.vn.backend.security.CurrentUserProvider;
import com.stu.edu.vn.backend.user.entity.*;
import com.stu.edu.vn.backend.user.enums.*;
import com.stu.edu.vn.backend.user.repository.*;
import com.stu.edu.vn.backend.user.service.UserPairLockCoordinator;
import jakarta.persistence.EntityManager;
import java.time.Clock;
import java.util.*;
import org.junit.jupiter.api.*;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.context.ApplicationEventPublisher;

/** Unit test các nhánh quyền, Follow, Block, Unicode và idempotency quan trọng nhất. */
class MessagingServiceImplTest {
    private final CurrentUserProvider currentUserProvider = mock(CurrentUserProvider.class);
    private final UserRepository userRepository = mock(UserRepository.class);
    private final UserProfileRepository profileRepository = mock(UserProfileRepository.class);
    private final UserBlockRepository blockRepository = mock(UserBlockRepository.class);
    private final FollowRepository followRepository = mock(FollowRepository.class);
    private final UserPairLockCoordinator pairLock = mock(UserPairLockCoordinator.class);
    private final ConversationRepository conversationRepository = mock(ConversationRepository.class);
    private final ConversationMemberRepository memberRepository = mock(ConversationMemberRepository.class);
    private final MessageRepository messageRepository = mock(MessageRepository.class);
    private final MessageAttachmentRepository attachmentRepository = mock(MessageAttachmentRepository.class);
    private final CursorCodec cursorCodec = mock(CursorCodec.class);
    private final EntityManager entityManager = mock(EntityManager.class);
    private final ApplicationEventPublisher eventPublisher = mock(ApplicationEventPublisher.class);
    private MessagingServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new MessagingServiceImpl(currentUserProvider, userRepository, profileRepository,
                blockRepository, followRepository, pairLock, conversationRepository, memberRepository,
                messageRepository, attachmentRepository, new MessagingMapper(), cursorCodec, entityManager,
                Clock.systemUTC(), eventPublisher);
        when(currentUserProvider.getCurrentUserId()).thenReturn(10L);
    }

    @Test
    void adminInactiveAndIncompleteUsersAreRejected() {
        User admin = user(10L, UserRole.ADMIN, UserStatus.ACTIVE);
        when(userRepository.findById(10L)).thenReturn(Optional.of(admin));
        when(profileRepository.existsByUserIdAndProfileCompletedAtIsNotNull(10L)).thenReturn(true);
        assertError(service::getUnreadCount, ErrorCode.MESSAGING_NOT_ALLOWED);

        User inactive = user(10L, UserRole.USER, UserStatus.BLOCKED);
        when(userRepository.findById(10L)).thenReturn(Optional.of(inactive));
        assertError(service::getUnreadCount, ErrorCode.MESSAGING_NOT_ALLOWED);

        User active = user(10L, UserRole.USER, UserStatus.ACTIVE);
        when(userRepository.findById(10L)).thenReturn(Optional.of(active));
        when(profileRepository.existsByUserIdAndProfileCompletedAtIsNotNull(10L)).thenReturn(false);
        assertError(service::getUnreadCount, ErrorCode.MESSAGING_NOT_ALLOWED);
    }

    @Test
    void cannotMessageSelfAndBlockPreventsOpen() {
        prepareEligible(10L);
        assertError(() -> service.openDirectConversation(10L), ErrorCode.CANNOT_MESSAGE_SELF);
        prepareEligible(20L);
        when(blockRepository.existsEitherDirection(10L, 20L)).thenReturn(true);
        assertError(() -> service.openDirectConversation(20L), ErrorCode.DIRECT_MESSAGE_NOT_ALLOWED);
        verify(pairLock).lockPair(10L, 20L);
    }

    @Test
    void openUsesRecipientFollowsSenderDirectionAndCreatesExactlyTwoMembers() {
        User sender = prepareEligible(10L);
        User recipient = prepareEligible(20L);
        when(blockRepository.existsEitherDirection(10L, 20L)).thenReturn(false);
        when(conversationRepository.findPairForUpdate(10L, 20L))
                .thenReturn(Optional.empty());
        when(followRepository.existsByIdFollowerIdAndIdFollowingId(20L, 10L)).thenReturn(true);
        when(conversationRepository.saveAndFlush(any())).thenAnswer(invocation -> {
            Conversation item = invocation.getArgument(0);
            ReflectionTestUtils.setField(item, "id", 99L);
            return item;
        });
        when(profileRepository.findById(20L)).thenReturn(Optional.of(profile(recipient, "Recipient")));

        assertThat(service.openDirectConversation(20L).conversationId()).isEqualTo(99L);
        verify(followRepository).existsByIdFollowerIdAndIdFollowingId(20L, 10L);
        verify(followRepository, never()).existsByIdFollowerIdAndIdFollowingId(10L, 20L);
        verify(memberRepository).saveAll(argThat(items -> {
            List<ConversationMember> members = new ArrayList<>();
            items.forEach(members::add);
            return members.size() == 2
                    && members.stream().map(item -> item.getUser().getId()).toList().containsAll(List.of(10L, 20L));
        }));
        assertThat(sender.getId()).isEqualTo(10L);
    }

    @Test
    void noFollowDoesNotCreateConversation() {
        prepareEligible(10L);
        prepareEligible(20L);
        when(conversationRepository.findPairForUpdate(10L, 20L))
                .thenReturn(Optional.empty());
        assertError(() -> service.openDirectConversation(20L), ErrorCode.DIRECT_MESSAGE_NOT_ALLOWED);
        verify(conversationRepository, never()).saveAndFlush(any());
    }

    @Test
    void blankAndMoreThan2000UnicodeCodePointsAreRejected() {
        prepareSendConversation();
        assertError(() -> service.sendMessage(50L,
                new SendMessageRequest("550e8400-e29b-41d4-a716-446655440000", "  \n ")),
                ErrorCode.MESSAGE_CONTENT_REQUIRED);
        assertError(() -> service.sendMessage(50L,
                new SendMessageRequest("550e8400-e29b-41d4-a716-446655440000", "😀".repeat(2001))),
                ErrorCode.MESSAGE_CONTENT_TOO_LONG);
    }

    @Test
    void emojiCountsAsCodePointAndUuidMustBeVersionFour() {
        Conversation conversation = prepareSendConversation();
        when(followRepository.existsByIdFollowerIdAndIdFollowingId(20L, 10L)).thenReturn(true);
        when(messageRepository.saveAndFlush(any())).thenAnswer(invocation -> {
            Message message = invocation.getArgument(0);
            ReflectionTestUtils.setField(message, "id", 70L);
            ReflectionTestUtils.setField(message, "createdAt", java.time.LocalDateTime.now());
            return message;
        });
        assertThat(service.sendMessage(50L, new SendMessageRequest(
                "550e8400-e29b-41d4-a716-446655440000", "😀".repeat(2000))).replayed()).isFalse();
        assertThat(conversation.getLastMessage().getId()).isEqualTo(70L);
        verify(eventPublisher).publishEvent(new MessageCreatedEvent(70L, 50L));

        assertError(() -> service.sendMessage(50L,
                new SendMessageRequest("550e8400-e29b-11d4-a716-446655440000", "ok")),
                ErrorCode.INVALID_CLIENT_MESSAGE_ID);
    }

    @Test
    void firstMessageRechecksFollowAfterEmptyConversation() {
        prepareSendConversation();
        when(followRepository.existsByIdFollowerIdAndIdFollowingId(20L, 10L)).thenReturn(false);
        assertError(() -> service.sendMessage(50L,
                new SendMessageRequest("550e8400-e29b-41d4-a716-446655440000", "hello")),
                ErrorCode.DIRECT_MESSAGE_NOT_ALLOWED);
        verify(messageRepository, never()).saveAndFlush(any());
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    void sameIdempotencyPayloadReplaysAndDifferentContentConflicts() {
        Conversation conversation = prepareSendConversation();
        Message old = new Message(conversation, user(10L, UserRole.USER, UserStatus.ACTIVE),
                "550e8400-e29b-41d4-a716-446655440000", "hello");
        ReflectionTestUtils.setField(old, "id", 80L);
        when(messageRepository.findBySenderAndClientMessageIdForUpdate(10L,
                "550e8400-e29b-41d4-a716-446655440000")).thenReturn(Optional.of(old));
        assertThat(service.sendMessage(50L, new SendMessageRequest(
                "550e8400-e29b-41d4-a716-446655440000", "hello")).replayed()).isTrue();
        assertError(() -> service.sendMessage(50L, new SendMessageRequest(
                "550e8400-e29b-41d4-a716-446655440000", "different")),
                ErrorCode.IDEMPOTENCY_KEY_REUSED);
        verify(messageRepository, never()).saveAndFlush(any());
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    void historyReversesDatabaseDescendingPageAndBuildsCursorFromOldestReturned() {
        User sender = prepareEligible(10L);
        User recipient = user(20L, UserRole.USER, UserStatus.ACTIVE);
        Conversation conversation = new Conversation(sender, recipient);
        ReflectionTestUtils.setField(conversation, "id", 50L);
        when(conversationRepository.findById(50L)).thenReturn(Optional.of(conversation));
        when(memberRepository.existsByIdConversationIdAndIdUserId(50L, 10L)).thenReturn(true);
        Message newest = message(conversation, sender, 3L, "newest");
        Message middle = message(conversation, recipient, 2L, "middle");
        Message oldestExtra = message(conversation, sender, 1L, "extra");
        when(messageRepository.findFirstPage(50L, 3)).thenReturn(List.of(newest, middle, oldestExtra));
        when(cursorCodec.encode(new MessageCursor(2L))).thenReturn("opaque-next");

        var page = service.getMessages(50L, 2, null);
        assertThat(page.content()).extracting(item -> item.messageId()).containsExactly(2L, 3L);
        assertThat(page.hasNext()).isTrue();
        assertThat(page.nextCursor()).isEqualTo("opaque-next");
    }

    @Test
    void staleReadMarkerIsIdempotentAndDoesNotChangeLastReadAt() {
        User current = prepareEligible(10L);
        User other = user(20L, UserRole.USER, UserStatus.ACTIVE);
        Conversation conversation = new Conversation(current, other);
        ReflectionTestUtils.setField(conversation, "id", 50L);
        ConversationMember member = new ConversationMember(conversation, current);
        Message currentMarker = message(conversation, other, 100L, "current");
        java.time.LocalDateTime originalReadAt = java.time.LocalDateTime.of(2026, 8, 1, 10, 0);
        member.advanceReadMarker(currentMarker, originalReadAt);
        Message stale = message(conversation, other, 90L, "stale");
        when(conversationRepository.findById(50L)).thenReturn(Optional.of(conversation));
        when(memberRepository.existsByIdConversationIdAndIdUserId(50L, 10L)).thenReturn(true);
        when(memberRepository.findForUpdate(50L, 10L)).thenReturn(Optional.of(member));
        when(messageRepository.findById(90L)).thenReturn(Optional.of(stale));
        when(messageRepository.countUnread(10L)).thenReturn(4L);

        var response = service.markRead(50L, new MarkConversationReadRequest(90L));
        assertThat(response.updated()).isFalse();
        assertThat(response.lastReadMessageId()).isEqualTo(100L);
        assertThat(response.lastReadAt()).isEqualTo(originalReadAt);
        assertThat(response.totalUnreadCount()).isEqualTo(4L);
        verify(memberRepository, never()).saveAndFlush(any());
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    void readMarkerAdvancesOnlyForMessageInSameConversation() {
        User current = prepareEligible(10L);
        User other = user(20L, UserRole.USER, UserStatus.ACTIVE);
        Conversation conversation = new Conversation(current, other);
        ReflectionTestUtils.setField(conversation, "id", 50L);
        ConversationMember member = new ConversationMember(conversation, current);
        when(conversationRepository.findById(50L)).thenReturn(Optional.of(conversation));
        when(memberRepository.existsByIdConversationIdAndIdUserId(50L, 10L)).thenReturn(true);
        when(memberRepository.findForUpdate(50L, 10L)).thenReturn(Optional.of(member));
        Message requested = message(conversation, other, 110L, "read");
        when(messageRepository.findById(110L)).thenReturn(Optional.of(requested));

        assertThat(service.markRead(50L, new MarkConversationReadRequest(110L)).updated()).isTrue();
        assertThat(member.getLastReadMessage().getId()).isEqualTo(110L);
        verify(memberRepository).saveAndFlush(member);
        verify(eventPublisher).publishEvent(new MessagesReadEvent(50L, 10L, 110L));

        Conversation another = new Conversation(current, user(30L, UserRole.USER, UserStatus.ACTIVE));
        ReflectionTestUtils.setField(another, "id", 60L);
        when(messageRepository.findById(120L)).thenReturn(Optional.of(message(another, current, 120L, "wrong")));
        assertError(() -> service.markRead(50L, new MarkConversationReadRequest(120L)),
                ErrorCode.MESSAGE_NOT_FOUND);
    }

    private Conversation prepareSendConversation() {
        User sender = prepareEligible(10L);
        User recipient = prepareEligible(20L);
        Conversation conversation = new Conversation(sender, recipient);
        ReflectionTestUtils.setField(conversation, "id", 50L);
        when(conversationRepository.findById(50L)).thenReturn(Optional.of(conversation));
        when(memberRepository.existsByIdConversationIdAndIdUserId(50L, 10L)).thenReturn(true);
        when(conversationRepository.findByIdForUpdate(50L)).thenReturn(Optional.of(conversation));
        return conversation;
    }

    private User prepareEligible(Long id) {
        User user = user(id, UserRole.USER, UserStatus.ACTIVE);
        when(userRepository.findById(id)).thenReturn(Optional.of(user));
        when(profileRepository.existsByUserIdAndProfileCompletedAtIsNotNull(id)).thenReturn(true);
        return user;
    }

    private User user(Long id, UserRole role, UserStatus status) {
        User user = new User("user" + id + "@example.com", "hash");
        ReflectionTestUtils.setField(user, "id", id);
        user.setRole(role);
        user.setStatus(status);
        return user;
    }

    private UserProfile profile(User user, String name) {
        UserProfile profile = new UserProfile(user);
        ReflectionTestUtils.setField(profile, "userId", user.getId());
        profile.setDisplayName(name);
        return profile;
    }

    private Message message(Conversation conversation, User sender, Long id, String content) {
        Message message = new Message(conversation, sender, UUID.randomUUID().toString(), content);
        ReflectionTestUtils.setField(message, "id", id);
        return message;
    }

    private void assertError(Runnable action, ErrorCode expected) {
        assertThatThrownBy(action::run).isInstanceOf(BusinessException.class)
                .extracting("errorCode").isEqualTo(expected);
    }
}
