package com.stu.edu.vn.backend.messaging.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import com.stu.edu.vn.backend.messaging.dto.request.TypingRequest;
import com.stu.edu.vn.backend.messaging.service.MessagingTypingService;
import java.security.Principal;
import org.junit.jupiter.api.Test;
import org.springframework.messaging.handler.annotation.MessageMapping;

class MessagingTypingControllerTest {
    private final MessagingTypingService service = mock(MessagingTypingService.class);
    private final MessagingTypingController controller = new MessagingTypingController(service);

    @Test
    void mappingUsesPrincipalAndNeverAcceptsSenderOrRecipientFromPayload() throws Exception {
        MessageMapping mapping = MessagingTypingController.class
                .getMethod("typing", TypingRequest.class, Principal.class)
                .getAnnotation(MessageMapping.class);
        assertThat(mapping.value()).containsExactly("/messaging/typing");

        TypingRequest request = new TypingRequest(15L, true);
        controller.typing(request, () -> "10");
        verify(service).handleTyping("10", request);

        controller.typing(request, null);
        verify(service).handleTyping(null, request);
    }
}
