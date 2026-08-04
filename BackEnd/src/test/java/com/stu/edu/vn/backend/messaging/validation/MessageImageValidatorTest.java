package com.stu.edu.vn.backend.messaging.validation;

import static org.assertj.core.api.Assertions.*;

import com.stu.edu.vn.backend.common.exception.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.*;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

/** Test file validation bằng dữ liệu ảnh thật, không chỉ dựa tên file hoặc header khai báo. */
class MessageImageValidatorTest {
    private final MessageImageValidator validator = new MessageImageValidator();

    @Test
    void acceptsOneAndFiveDecodedImagesWithStrongHashes() throws Exception {
        MultipartFile image = png("one.png");
        ValidatedMessageImage validated = validator.validate(List.of(image)).getFirst();
        assertThat(validated.actualMimeType()).isEqualTo("image/png");
        assertThat(validated.width()).isEqualTo(2);
        assertThat(validated.height()).isEqualTo(3);
        assertThat(validated.sha256()).hasSize(64);
        assertThat(validator.validate(List.of(png("1.png"), png("2.png"), png("3.png"),
                png("4.png"), png("5.png"))).size()).isEqualTo(5);
    }

    @Test
    void rejectsSixImagesAndEmptyFile() throws Exception {
        List<MultipartFile> six = new ArrayList<>();
        for (int i = 0; i < 6; i++) six.add(png(i + ".png"));
        assertError(() -> validator.validate(six), ErrorCode.MESSAGE_IMAGE_LIMIT_EXCEEDED);
        assertError(() -> validator.validate(List.of(
                new MockMultipartFile("images", "empty.png", "image/png", new byte[0]))),
                ErrorCode.MESSAGE_IMAGE_FILE_EMPTY);
    }

    @Test
    void rejectsOversizedImageBeforeDecode() {
        byte[] oversized = new byte[10 * 1024 * 1024 + 1];
        assertError(() -> validator.validate(List.of(
                new MockMultipartFile("images", "large.png", "image/png", oversized))),
                ErrorCode.MESSAGE_IMAGE_TOO_LARGE);
    }

    @Test
    void acceptsSupportedMetadataMismatchButRejectsFakeMagicBytes() throws Exception {
        byte[] png = pngBytes();
        ValidatedMessageImage normalized = validator.validate(List.of(
                new MockMultipartFile("images", "renamed.jpg", "image/jpeg", png))).getFirst();
        assertThat(normalized.actualMimeType()).isEqualTo("image/png");
        assertThat(validator.validate(List.of(
                new MockMultipartFile("images", "camera.jpg", "image/jpg", jpegBytes()))).getFirst()
                .actualMimeType()).isEqualTo("image/jpeg");
        assertError(() -> validator.validate(List.of(
                new MockMultipartFile("images", "fake.png", "image/png", "not-image".getBytes()))),
                ErrorCode.MESSAGE_IMAGE_SIGNATURE_INVALID);
    }

    @Test
    void rejectsUnsupportedDeclaredMimeEvenWhenExtensionIsAllowed() throws Exception {
        assertError(() -> validator.validate(List.of(
                new MockMultipartFile("images", "image.png", "application/octet-stream", pngBytes()))),
                ErrorCode.MESSAGE_IMAGE_MIME_TYPE_INVALID);
    }

    @Test
    void rejectsHeaderOnlyImageThatCannotBeDecoded() {
        byte[] headerOnly = {(byte) 0x89, 0x50, 0x4e, 0x47, 0x0d, 0x0a, 0x1a, 0x0a};
        assertError(() -> validator.validate(List.of(
                new MockMultipartFile("images", "broken.png", "image/png", headerOnly))),
                ErrorCode.MESSAGE_IMAGE_DECODE_INVALID);
    }

    private MultipartFile png(String filename) throws Exception {
        return new MockMultipartFile("images", filename, "image/png", pngBytes());
    }

    private byte[] pngBytes() throws Exception {
        BufferedImage image = new BufferedImage(2, 3, BufferedImage.TYPE_INT_RGB);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        ImageIO.write(image, "png", output);
        return output.toByteArray();
    }

    private byte[] jpegBytes() throws Exception {
        BufferedImage image = new BufferedImage(2, 3, BufferedImage.TYPE_INT_RGB);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        ImageIO.write(image, "jpeg", output);
        return output.toByteArray();
    }

    private void assertError(org.assertj.core.api.ThrowableAssert.ThrowingCallable action, ErrorCode code) {
        assertThatThrownBy(action).isInstanceOf(BusinessException.class)
                .extracting(error -> ((BusinessException) error).getErrorCode()).isEqualTo(code);
    }
}
