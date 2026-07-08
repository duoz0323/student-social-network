package com.stu.edu.vn.backend.post.validation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.stu.edu.vn.backend.common.exception.BusinessException;
import com.stu.edu.vn.backend.common.exception.ErrorCode;
import org.junit.jupiter.api.Test;

class PostValidationSupportTest {

    private final PostValidationSupport validationSupport = new PostValidationSupport();

    @Test
    void normalizeContentReturnsNullForNullEmptyOrBlankContent() {
        // Nội dung không có giá trị sau trim được xem là không có content.
        assertThat(validationSupport.normalizeContent(null)).isNull();
        assertThat(validationSupport.normalizeContent("")).isNull();
        assertThat(validationSupport.normalizeContent("   ")).isNull();
    }

    @Test
    void normalizeContentAcceptsExactlyFiveHundredCharacters() {
        // Giới hạn 500 ký tự khớp cột posts.content trong database.
        String content = "a".repeat(500);

        assertThat(validationSupport.normalizeContent(content)).hasSize(500);
    }

    @Test
    void normalizeContentRejectsFiveHundredOneCharacters() {
        // Nội dung vượt 500 ký tự phải bị từ chối trước khi lưu database.
        String content = "a".repeat(501);

        assertThatThrownBy(() -> validationSupport.normalizeContent(content))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.POST_CONTENT_TOO_LONG);
    }

    @Test
    void validateForCreateRejectsPostWithoutContentAndImages() {
        // Bài viết MVP phải có ít nhất nội dung sau trim hoặc một ảnh hợp lệ.
        assertThatThrownBy(() -> validationSupport.validateForCreate("   ", 0))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.POST_CONTENT_OR_IMAGE_REQUIRED);
    }

    @Test
    void validateForCreateAcceptsContentOrImage() {
        // Có content hoặc có ảnh đều đủ điều kiện tối thiểu để tạo bài.
        assertThat(validationSupport.validateForCreate("  Xin chao  ", 0)).isEqualTo("Xin chao");
        assertThat(validationSupport.validateForCreate(null, 1)).isNull();
    }
}
