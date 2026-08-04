package com.stu.edu.vn.backend.messaging.controller;

import com.stu.edu.vn.backend.messaging.dto.request.TypingRequest;
import com.stu.edu.vn.backend.messaging.service.MessagingTypingService;
import java.security.Principal;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

/** Điểm nhận SEND duy nhất của client; mọi mutation bền vững vẫn đi qua REST. */
@Controller
@RequiredArgsConstructor
public class MessagingTypingController {
    private final MessagingTypingService messagingTypingService;

    @MessageMapping("/messaging/typing")
    public void typing(TypingRequest request, Principal principal) {
        messagingTypingService.handleTyping(principal == null ? null : principal.getName(), request);
    }
}
