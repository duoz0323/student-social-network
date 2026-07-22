package com.stu.edu.vn.backend.post.validation;

import com.stu.edu.vn.backend.common.exception.BusinessException;
import com.stu.edu.vn.backend.common.exception.ErrorCode;
import org.springframework.stereotype.Component;

/**
 * Validation nội dung bài viết, tách khỏi Service để có thể kiểm thử độc lập.
 */
@Component
public class PostValidationSupport {

    private static final int MAX_CONTENT_LENGTH = 500;

    public String normalizeContent(String content) {
        // Nội dung rỗng sau trim được lưu như NULL để kiểm tra bài có ảnh hoặc nội dung rõ ràng.
        if (content == null) {
            return null;
        }
        String normalizedContent = content.trim();
        if (normalizedContent.isEmpty()) {
            return null;
        }
        if (normalizedContent.length() > MAX_CONTENT_LENGTH) {
            throw new BusinessException(ErrorCode.POST_CONTENT_TOO_LONG);
        }
        return normalizedContent;
    }

    public String validateForCreate(String content, int imageCount) {
        // Tạo bài yêu cầu có nội dung sau trim hoặc ít nhất một ảnh hợp lệ.
        String normalizedContent = normalizeContent(content);
        validateContentOrImagePresent(normalizedContent, imageCount);
        return normalizedContent;
    }

    public void validateContentOrImagePresent(String normalizedContent, int imageCount) {
        // Điều kiện tối thiểu của bài viết MVP: không cho tạo bài hoàn toàn rỗng.
        if ((normalizedContent == null || normalizedContent.isBlank()) && imageCount <= 0) {
            throw new BusinessException(ErrorCode.POST_CONTENT_OR_IMAGE_REQUIRED);
        }
    }
}
