package com.stu.edu.vn.backend.post.validation;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.stu.edu.vn.backend.common.exception.BusinessException;
import com.stu.edu.vn.backend.common.exception.ErrorCode;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

class PostImageFileValidatorTest {

    private final PostImageFileValidator validator = new PostImageFileValidator();

    @Test
    void validateAcceptsZeroOneAndFourImages() {
        // 0 ảnh hợp lệ ở validator ảnh; điều kiện cần content sẽ do PostValidationSupport kiểm tra.
        validator.validate(List.of());
        validator.validate(List.of(png("one.png")));
        validator.validate(List.of(png("1.png"), jpeg("2.jpg"), webp("3.webp"), png("4.png")));
    }

    @Test
    void validateRejectsFiveImages() {
        // MVP chỉ cho tối đa 4 ảnh trong một bài viết.
        List<MultipartFile> files = List.of(png("1.png"), png("2.png"), png("3.png"), png("4.png"), png("5.png"));

        assertThatThrownBy(() -> validator.validate(files))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.POST_IMAGE_LIMIT_EXCEEDED);
    }

    @Test
    void validateRejectsEmptyFile() {
        // Multipart có part ảnh nhưng không có nội dung phải bị từ chối.
        MockMultipartFile empty = new MockMultipartFile("images", "empty.png", "image/png", new byte[0]);

        assertThatThrownBy(() -> validator.validate(List.of(empty)))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.POST_IMAGE_FILE_EMPTY);
    }

    @Test
    void validateRejectsImageLargerThanTenMegabytes() {
        // Dung lượng mỗi ảnh bị giới hạn ở 10 MB.
        byte[] bytes = new byte[(10 * 1024 * 1024) + 1];
        bytes[0] = (byte) 0x89;
        bytes[1] = 0x50;
        bytes[2] = 0x4E;
        bytes[3] = 0x47;
        bytes[4] = 0x0D;
        bytes[5] = 0x0A;
        bytes[6] = 0x1A;
        bytes[7] = 0x0A;
        MockMultipartFile large = new MockMultipartFile("images", "large.png", "image/png", bytes);

        assertThatThrownBy(() -> validator.validate(List.of(large)))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.POST_IMAGE_TOO_LARGE);
    }

    @Test
    void validateRejectsInvalidExtension() {
        // Extension phải thuộc JPG, JPEG, PNG hoặc WEBP.
        MockMultipartFile file = new MockMultipartFile("images", "note.gif", "image/png", pngBytes());

        assertThatThrownBy(() -> validator.validate(List.of(file)))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.POST_IMAGE_EXTENSION_NOT_ALLOWED);
    }

    @Test
    void validateRejectsInvalidMimeType() {
        // MIME type multipart phải khớp danh sách image/jpeg, image/png hoặc image/webp.
        MockMultipartFile file = new MockMultipartFile("images", "image.png", "text/plain", pngBytes());

        assertThatThrownBy(() -> validator.validate(List.of(file)))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.POST_IMAGE_MIME_TYPE_INVALID);
    }

    @Test
    void validateRejectsFakeImageSignature() {
        // File giả ảnh bị phát hiện bằng chữ ký byte đầu, dù extension và MIME hợp lệ.
        MockMultipartFile file = new MockMultipartFile("images", "fake.png", "image/png", "not-image".getBytes());

        assertThatThrownBy(() -> validator.validate(List.of(file)))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.POST_IMAGE_SIGNATURE_INVALID);
    }

    private MockMultipartFile png(String filename) {
        return new MockMultipartFile("images", filename, "image/png", pngBytes());
    }

    private MockMultipartFile jpeg(String filename) {
        return new MockMultipartFile("images", filename, "image/jpeg", new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF});
    }

    private MockMultipartFile webp(String filename) {
        return new MockMultipartFile(
                "images",
                filename,
                "image/webp",
                new byte[]{0x52, 0x49, 0x46, 0x46, 0, 0, 0, 0, 0x57, 0x45, 0x42, 0x50}
        );
    }

    private byte[] pngBytes() {
        return new byte[]{(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A};
    }
}
