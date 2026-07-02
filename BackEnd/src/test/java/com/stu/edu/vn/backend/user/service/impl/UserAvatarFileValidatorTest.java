package com.stu.edu.vn.backend.user.service.impl;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.stu.edu.vn.backend.common.exception.BusinessException;
import com.stu.edu.vn.backend.common.exception.ErrorCode;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

class UserAvatarFileValidatorTest {

    private final UserAvatarFileValidator validator = new UserAvatarFileValidator();

    @Test
    void validateAcceptsValidPngFile() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "avatar.png",
                "image/png",
                new byte[]{(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A}
        );

        validator.validate(file);
    }

    @Test
    void validateRejectsEmptyFile() {
        MockMultipartFile file = new MockMultipartFile("file", "avatar.png", "image/png", new byte[0]);

        assertThatThrownBy(() -> validator.validate(file))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.AVATAR_FILE_REQUIRED);
    }

    @Test
    void validateRejectsUnsupportedExtension() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "avatar.gif",
                "image/gif",
                new byte[]{0x47, 0x49, 0x46}
        );

        assertThatThrownBy(() -> validator.validate(file))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.AVATAR_FILE_TYPE_NOT_ALLOWED);
    }

    @Test
    void validateRejectsFakeImageContent() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "avatar.png",
                "image/png",
                "not-an-image".getBytes()
        );

        assertThatThrownBy(() -> validator.validate(file))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.AVATAR_MIME_TYPE_INVALID);
    }
}
