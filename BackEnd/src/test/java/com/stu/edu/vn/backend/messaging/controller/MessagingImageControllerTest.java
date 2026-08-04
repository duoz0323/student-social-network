package com.stu.edu.vn.backend.messaging.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.stu.edu.vn.backend.common.exception.GlobalExceptionHandler;
import com.stu.edu.vn.backend.messaging.dto.response.*;
import com.stu.edu.vn.backend.messaging.enums.MessageType;
import com.stu.edu.vn.backend.messaging.service.MessagingImageService;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.*;
import org.mockito.ArgumentCaptor;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.*;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

/** Contract multipart giữ nguyên path và không nhận sender/type/storage identifier. */
class MessagingImageControllerTest {
    private final MessagingImageService service = mock(MessagingImageService.class);
    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.standaloneSetup(new MessagingImageController(service))
                .setControllerAdvice(new GlobalExceptionHandler()).build();
    }

    @Test
    void multipartDelegatesOnlyClientKeyCaptionAndImagesAndReturns201() throws Exception {
        MessageResponse message = new MessageResponse(9L, 2L, 10L,
                "550e8400-e29b-41d4-a716-446655440000", MessageType.IMAGE, "caption", List.of(),
                LocalDateTime.now());
        when(service.sendImageMessage(eq(2L), any())).thenReturn(new SendMessageResponse(message, false));

        mvc.perform(multipart("/api/v1/conversations/2/messages")
                        .file(textPart("clientMessageId", "550e8400-e29b-41d4-a716-446655440000"))
                        .file(textPart("content", "caption"))
                        .file(new MockMultipartFile("images", "photo.png", "image/png", new byte[]{1})))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.message.type").value("IMAGE"));

        ArgumentCaptor<com.stu.edu.vn.backend.messaging.dto.request.SendImageMessageRequest> captor =
                ArgumentCaptor.forClass(com.stu.edu.vn.backend.messaging.dto.request.SendImageMessageRequest.class);
        verify(service).sendImageMessage(eq(2L), captor.capture());
        org.assertj.core.api.Assertions.assertThat(captor.getValue().images()).hasSize(1);
    }

    @Test
    void replayReturns200() throws Exception {
        MessageResponse message = new MessageResponse(9L, 2L, 10L,
                "550e8400-e29b-41d4-a716-446655440000", MessageType.IMAGE, null, List.of(),
                LocalDateTime.now());
        when(service.sendImageMessage(eq(2L), any())).thenReturn(new SendMessageResponse(message, true));
        mvc.perform(multipart("/api/v1/conversations/2/messages")
                        .file(textPart("clientMessageId", "550e8400-e29b-41d4-a716-446655440000"))
                        .file(new MockMultipartFile("images", "photo.png", "image/png", new byte[]{1})))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.replayed").value(true));
    }

    private MockMultipartFile textPart(String name, String value) {
        return new MockMultipartFile(name, "", "text/plain", value.getBytes(StandardCharsets.UTF_8));
    }
}
