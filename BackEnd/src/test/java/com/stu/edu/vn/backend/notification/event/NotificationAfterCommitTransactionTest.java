package com.stu.edu.vn.backend.notification.event;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.stu.edu.vn.backend.notification.dto.response.NotificationResponse;
import com.stu.edu.vn.backend.notification.enums.NotificationType;
import com.stu.edu.vn.backend.notification.mapper.NotificationMapper;
import com.stu.edu.vn.backend.notification.repository.NotificationRepository;
import com.stu.edu.vn.backend.notification.repository.projection.NotificationListProjection;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Optional;
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

/**
 * Xác minh listener thực sự tuân theo transaction lifecycle mà không phụ thuộc database ngoài.
 */
class NotificationAfterCommitTransactionTest {

    private AnnotationConfigApplicationContext context;
    private TransactionalPublisher publisher;
    private SimpMessagingTemplate messagingTemplate;
    private NotificationRepository notificationRepository;

    @BeforeEach
    void setUp() {
        context = new AnnotationConfigApplicationContext(TestConfig.class);
        publisher = context.getBean(TransactionalPublisher.class);
        messagingTemplate = context.getBean(SimpMessagingTemplate.class);
        notificationRepository = context.getBean(NotificationRepository.class);
        reset(messagingTemplate, notificationRepository);

        NotificationListProjection projection = org.mockito.Mockito.mock(NotificationListProjection.class);
        when(notificationRepository.findVisibleNotificationForRealtime(99L, 20L))
                .thenReturn(Optional.of(projection));
        when(notificationRepository.countByRecipient_IdAndReadAtIsNullAndDeletedAtIsNull(20L))
                .thenReturn(1L);
    }

    @org.junit.jupiter.api.AfterEach
    void tearDown() {
        context.close();
    }

    @Test
    void eventIsNotSentBeforeCommitAndIsSentAfterSuccessfulCommit() {
        publisher.publishAndAssertNotSent(() ->
                verify(messagingTemplate, never()).convertAndSendToUser(any(), any(), any()));

        verify(messagingTemplate).convertAndSendToUser(
                org.mockito.ArgumentMatchers.eq("20"),
                org.mockito.ArgumentMatchers.eq("/queue/notifications"),
                any()
        );
    }

    @Test
    void rollbackDoesNotSendRealtimeEvent() {
        assertThatThrownBy(publisher::publishAndRollback)
                .isInstanceOf(IllegalStateException.class);

        verify(messagingTemplate, never()).convertAndSendToUser(any(), any(), any());
    }

    @Test
    void brokerFailureCannotFailCommittedBusinessTransaction() {
        org.mockito.Mockito.doThrow(new IllegalStateException("broker unavailable"))
                .when(messagingTemplate).convertAndSendToUser(any(), any(), any());

        assertThatCode(() -> publisher.publishAndAssertNotSent(() -> { }))
                .doesNotThrowAnyException();
    }

    @Configuration
    @EnableTransactionManagement
    static class TestConfig {

        @Bean
        PlatformTransactionManager transactionManager() {
            return new NoOpTransactionManager();
        }

        @Bean
        NotificationRepository notificationRepository() {
            return org.mockito.Mockito.mock(NotificationRepository.class);
        }

        @Bean
        NotificationMapper notificationMapper() {
            NotificationMapper mapper = org.mockito.Mockito.mock(NotificationMapper.class);
            when(mapper.toResponse(any())).thenReturn(new NotificationResponse(
                    99L, NotificationType.FOLLOW, null, null, null, null, null,
                    LocalDateTime.of(2026, 7, 30, 10, 0)));
            return mapper;
        }

        @Bean
        SimpMessagingTemplate messagingTemplate() {
            return org.mockito.Mockito.mock(SimpMessagingTemplate.class);
        }

        @Bean
        Clock clock() {
            return Clock.systemUTC();
        }

        @Bean
        NotificationRealtimeListener listener(
                NotificationRepository repository,
                NotificationMapper mapper,
                SimpMessagingTemplate template,
                Clock clock
        ) {
            return new NotificationRealtimeListener(repository, mapper, template, clock);
        }

        @Bean
        TransactionalPublisher publisher(ApplicationEventPublisher eventPublisher) {
            return new TransactionalPublisher(eventPublisher);
        }
    }

    static class TransactionalPublisher {
        private final ApplicationEventPublisher eventPublisher;

        TransactionalPublisher(ApplicationEventPublisher eventPublisher) {
            this.eventPublisher = eventPublisher;
        }

        @Transactional
        public void publishAndAssertNotSent(Runnable assertionBeforeCommit) {
            eventPublisher.publishEvent(new NotificationCreatedEvent(99L, 20L));
            assertionBeforeCommit.run();
        }

        @Transactional
        public void publishAndRollback() {
            eventPublisher.publishEvent(new NotificationCreatedEvent(99L, 20L));
            throw new IllegalStateException("rollback");
        }
    }

    static class NoOpTransactionManager extends AbstractPlatformTransactionManager {
        @Override
        protected Object doGetTransaction() {
            return new Object();
        }

        @Override
        protected void doBegin(Object transaction, TransactionDefinition definition) {
            // Transaction giả chỉ phục vụ kiểm tra callback AFTER_COMMIT của Spring.
        }

        @Override
        protected void doCommit(DefaultTransactionStatus status) {
            // Không có resource vật lý cần commit.
        }

        @Override
        protected void doRollback(DefaultTransactionStatus status) {
            // Không có resource vật lý cần rollback.
        }
    }
}
