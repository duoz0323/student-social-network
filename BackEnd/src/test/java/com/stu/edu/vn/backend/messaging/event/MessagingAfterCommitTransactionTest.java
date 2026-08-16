package com.stu.edu.vn.backend.messaging.event;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import com.stu.edu.vn.backend.messaging.projection.MessageRealtimeProjection;
import com.stu.edu.vn.backend.messaging.repository.ConversationMemberRepository;
import com.stu.edu.vn.backend.messaging.repository.MessageRepository;
import com.stu.edu.vn.backend.messaging.repository.MessageAttachmentRepository;
import com.stu.edu.vn.backend.messaging.service.SharedPostMessageLoader;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.AbstractPlatformTransactionManager;
import org.springframework.transaction.support.DefaultTransactionStatus;

/** Xác minh callback AFTER_COMMIT thật của Spring, độc lập database vật lý. */
class MessagingAfterCommitTransactionTest {
    private AnnotationConfigApplicationContext context;
    private TransactionalPublisher publisher;
    private SimpMessagingTemplate template;

    @BeforeEach
    void setUp() {
        context = new AnnotationConfigApplicationContext(TestConfig.class);
        publisher = context.getBean(TransactionalPublisher.class);
        template = context.getBean(SimpMessagingTemplate.class);
        MessageRepository repository = context.getBean(MessageRepository.class);
        MessageRealtimeProjection row = mock(MessageRealtimeProjection.class);
        when(row.getMessageId()).thenReturn(901L);
        when(row.getConversationId()).thenReturn(15L);
        when(row.getSenderId()).thenReturn(10L);
        when(row.getClientMessageId()).thenReturn("550e8400-e29b-41d4-a716-446655440000");
        when(row.getType()).thenReturn("TEXT");
        when(row.getContent()).thenReturn("hello");
        when(row.getCreatedAt()).thenReturn(LocalDateTime.of(2026, 8, 3, 10, 0));
        when(row.getParticipantLowId()).thenReturn(10L);
        when(row.getParticipantHighId()).thenReturn(20L);
        when(repository.findVisibleMessageForRealtime(901L)).thenReturn(Optional.of(row));
    }

    @AfterEach
    void tearDown() { context.close(); }

    @Test
    void eventIsInvisibleBeforeCommitThenSentToBothUsers() {
        publisher.publishAndAssertBeforeCommit(() -> verifyNoInteractions(template));
        verify(template).convertAndSendToUser(eq("10"), eq("/queue/messaging"), any());
        verify(template).convertAndSendToUser(eq("20"), eq("/queue/messaging"), any());
    }

    @Test
    void rollbackSuppressesCreatedAndReadEvents() {
        assertThatThrownBy(publisher::publishBothAndRollback).isInstanceOf(IllegalStateException.class);
        verifyNoInteractions(template);
    }

    @Configuration
    @EnableTransactionManagement
    static class TestConfig {
        @Bean PlatformTransactionManager transactionManager() { return new NoOpTransactionManager(); }
        @Bean MessageRepository messageRepository() { return mock(MessageRepository.class); }
        @Bean ConversationMemberRepository memberRepository() { return mock(ConversationMemberRepository.class); }
        @Bean MessageAttachmentRepository attachmentRepository() { return mock(MessageAttachmentRepository.class); }
        @Bean SharedPostMessageLoader sharedPostMessageLoader() { return mock(SharedPostMessageLoader.class); }
        @Bean SimpMessagingTemplate template() { return mock(SimpMessagingTemplate.class); }
        @Bean Clock clock() { return Clock.systemUTC(); }
        @Bean MessagingRealtimeListener listener(MessageRepository messages, ConversationMemberRepository members,
                MessageAttachmentRepository attachments, SharedPostMessageLoader sharedPosts,
                SimpMessagingTemplate template, Clock clock) {
            return new MessagingRealtimeListener(messages, members, attachments, sharedPosts, template, clock);
        }
        @Bean TransactionalPublisher publisher(ApplicationEventPublisher events) { return new TransactionalPublisher(events); }
    }

    static class TransactionalPublisher {
        private final ApplicationEventPublisher events;
        TransactionalPublisher(ApplicationEventPublisher events) { this.events = events; }
        @Transactional
        public void publishAndAssertBeforeCommit(Runnable assertion) {
            events.publishEvent(new MessageCreatedEvent(901L, 15L));
            assertion.run();
        }
        @Transactional
        public void publishBothAndRollback() {
            events.publishEvent(new MessageCreatedEvent(901L, 15L));
            events.publishEvent(new MessagesReadEvent(15L, 20L, 901L));
            throw new IllegalStateException("rollback");
        }
    }

    static class NoOpTransactionManager extends AbstractPlatformTransactionManager {
        @Override protected Object doGetTransaction() { return new Object(); }
        @Override protected void doBegin(Object transaction, TransactionDefinition definition) { }
        @Override protected void doCommit(DefaultTransactionStatus status) { }
        @Override protected void doRollback(DefaultTransactionStatus status) { }
    }
}
