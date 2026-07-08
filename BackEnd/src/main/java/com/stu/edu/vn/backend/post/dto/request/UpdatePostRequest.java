package com.stu.edu.vn.backend.post.dto.request;

import java.util.List;
import org.springframework.web.multipart.MultipartFile;

/**
 * Request cập nhật bài viết dạng multipart, cho phép thay nội dung, hashtag và ảnh trong giới hạn nghiệp vụ.
 */
public record UpdatePostRequest(
        String content,
        List<String> hashtags,
        List<Long> keepMediaIds,
        List<MultipartFile> newImages
) {
}
