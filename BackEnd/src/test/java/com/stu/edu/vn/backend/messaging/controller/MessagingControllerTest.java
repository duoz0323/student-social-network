package com.stu.edu.vn.backend.messaging.controller;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.stu.edu.vn.backend.common.api.CursorPageResponse;
import com.stu.edu.vn.backend.common.exception.GlobalExceptionHandler;
import com.stu.edu.vn.backend.common.exception.BusinessException;
import com.stu.edu.vn.backend.common.exception.ErrorCode;
import com.stu.edu.vn.backend.messaging.dto.response.*;
import com.stu.edu.vn.backend.messaging.enums.MessageType;
import com.stu.edu.vn.backend.messaging.service.MessagingService;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.*;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

/** Contract HTTP bảo vệ status insert/replay và request không nhận sender/type. */
class MessagingControllerTest {
    private final MessagingService service = mock(MessagingService.class);
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new MessagingController(service))
                .setControllerAdvice(new GlobalExceptionHandler()).build();
    }

    @Test
    void listUsesCursorDefaultsAndUnreadHasDedicatedRoute() throws Exception {
        when(service.getConversations(20, null)).thenReturn(new CursorPageResponse<>(List.of(), null, false));
        when(service.getUnreadCount()).thenReturn(new MessagingUnreadCountResponse(8));
        mockMvc.perform(get("/api/v1/conversations"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data.hasNext").value(false));
        mockMvc.perform(get("/api/v1/conversations/unread-count"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data.unreadCount").value(8));
    }

    @Test
    void newSendReturns201AndReplayReturns200() throws Exception {
        MessageResponse message = new MessageResponse(3L, 2L, 1L,
                "550e8400-e29b-41d4-a716-446655440000", MessageType.TEXT, "hello", LocalDateTime.now());
        when(service.sendMessage(eq(2L), any())).thenReturn(new SendMessageResponse(message, false));
        String body = "{\"clientMessageId\":\"550e8400-e29b-41d4-a716-446655440000\",\"content\":\"hello\"}";
        mockMvc.perform(post("/api/v1/conversations/2/messages").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.data.replayed").value(false));
        when(service.sendMessage(eq(2L), any())).thenReturn(new SendMessageResponse(message, true));
        mockMvc.perform(post("/api/v1/conversations/2/messages").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data.replayed").value(true));
    }

    @Test
    void paginationLimitsAreValidatedAtBoundary() throws Exception {
        when(service.getConversations(51, null)).thenThrow(new BusinessException(ErrorCode.VALIDATION_ERROR));
        when(service.getMessages(1L, 101, null)).thenThrow(new BusinessException(ErrorCode.VALIDATION_ERROR));
        mockMvc.perform(get("/api/v1/conversations").param("limit", "51"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
        mockMvc.perform(get("/api/v1/conversations/1/messages").param("limit", "101"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }
}
