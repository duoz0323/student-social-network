package com.stu.edu.vn.backend.messaging.dto.request;

import java.util.List;
import org.springframework.web.multipart.MultipartFile;

/** Multipart command không nhận sender, message type hay định danh storage từ Client. */
public record SendImageMessageRequest(String clientMessageId, String content, List<MultipartFile> images) {
}
