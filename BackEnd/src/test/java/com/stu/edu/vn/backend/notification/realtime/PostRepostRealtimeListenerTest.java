package com.stu.edu.vn.backend.notification.realtime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

import com.stu.edu.vn.backend.notification.event.PostRepostNotificationEvent;
import java.lang.reflect.Method;
import org.junit.jupiter.api.Test;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/** Bảo vệ contract realtime: listener chỉ được Spring gọi sau khi transaction commit. */
class PostRepostRealtimeListenerTest {

    @Test
    void listenerMustBeAfterCommitAndPublishToRecipientQueue() throws Exception {
        SimpMessagingTemplate template = org.mockito.Mockito.mock(SimpMessagingTemplate.class);
        PostRepostRealtimeListener listener = new PostRepostRealtimeListener(template);
        Method method = PostRepostRealtimeListener.class
                .getMethod("onPostRepost", PostRepostNotificationEvent.class);
        TransactionalEventListener annotation = method.getAnnotation(TransactionalEventListener.class);

        assertThat(annotation).isNotNull();
        assertThat(annotation.phase()).isEqualTo(TransactionPhase.AFTER_COMMIT);
        assertThat(annotation.fallbackExecution()).isFalse();
        listener.onPostRepost(new PostRepostNotificationEvent(10L, 20L, 100L));
        verify(template).convertAndSendToUser(org.mockito.ArgumentMatchers.eq("20"),
                org.mockito.ArgumentMatchers.eq("/queue/notifications"), any());
    }
}
