package com.stu.edu.vn.backend.messaging.validation;

import com.stu.edu.vn.backend.common.exception.BusinessException;
import com.stu.edu.vn.backend.common.exception.ErrorCode;
import java.awt.image.BufferedImage;
import java.io.*;
import java.security.MessageDigest;
import java.util.*;
import javax.imageio.ImageIO;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

/** Kiểm tra extension, MIME khai báo, chữ ký, khả năng giải mã và kích thước ảnh trước upload. */
@Component
public class MessageImageValidator {
    private static final int MAX_IMAGE_COUNT = 5;
    private static final long MAX_IMAGE_SIZE_BYTES = 10L * 1024L * 1024L;
    private static final Map<String, String> EXTENSION_MIME = Map.of(
            "jpg", "image/jpeg", "jpeg", "image/jpeg", "png", "image/png", "webp", "image/webp");

    public List<ValidatedMessageImage> validate(List<MultipartFile> files) {
        List<MultipartFile> safeFiles = files == null ? List.of() : files;
        if (safeFiles.size() > MAX_IMAGE_COUNT) {
            throw new BusinessException(ErrorCode.MESSAGE_IMAGE_LIMIT_EXCEEDED);
        }
        List<ValidatedMessageImage> result = new ArrayList<>(safeFiles.size());
        for (MultipartFile file : safeFiles) {
            result.add(validateOne(file));
        }
        return List.copyOf(result);
    }

    private ValidatedMessageImage validateOne(MultipartFile file) {
        if (file == null || file.isEmpty() || file.getSize() <= 0) {
            throw new BusinessException(ErrorCode.MESSAGE_IMAGE_FILE_EMPTY);
        }
        if (file.getSize() > MAX_IMAGE_SIZE_BYTES) {
            throw new BusinessException(ErrorCode.MESSAGE_IMAGE_TOO_LARGE);
        }
        String expectedMime = EXTENSION_MIME.get(extensionOf(file.getOriginalFilename()));
        if (expectedMime == null) {
            throw new BusinessException(ErrorCode.MESSAGE_IMAGE_EXTENSION_NOT_ALLOWED);
        }
        String declaredMime = normalizeDeclaredMime(file.getContentType());
        if (!EXTENSION_MIME.containsValue(declaredMime)) {
            throw new BusinessException(ErrorCode.MESSAGE_IMAGE_MIME_TYPE_INVALID);
        }
        try {
            byte[] bytes = file.getBytes();
            String actualMime = detectActualMime(bytes);
            // Magic bytes là nguồn sự thật; tên hoặc MIME do trình duyệt gắn sai không được làm hỏng ảnh hợp lệ.
            int[] dimensions = dimensions(bytes, actualMime);
            if (dimensions[0] <= 0 || dimensions[1] <= 0) {
                throw new BusinessException(ErrorCode.MESSAGE_IMAGE_DECODE_INVALID);
            }
            return new ValidatedMessageImage(file, actualMime, bytes.length, dimensions[0], dimensions[1], sha256(bytes));
        } catch (IOException exception) {
            throw new BusinessException(ErrorCode.MESSAGE_IMAGE_DECODE_INVALID);
        }
    }

    private String detectActualMime(byte[] bytes) {
        if (bytes.length >= 3 && unsigned(bytes[0]) == 0xFF && unsigned(bytes[1]) == 0xD8 && unsigned(bytes[2]) == 0xFF) {
            return "image/jpeg";
        }
        if (bytes.length >= 8 && unsigned(bytes[0]) == 0x89 && bytes[1] == 0x50 && bytes[2] == 0x4E
                && bytes[3] == 0x47 && bytes[4] == 0x0D && bytes[5] == 0x0A && bytes[6] == 0x1A && bytes[7] == 0x0A) {
            return "image/png";
        }
        if (bytes.length >= 16 && ascii(bytes, 0, "RIFF") && ascii(bytes, 8, "WEBP")) {
            return "image/webp";
        }
        throw new BusinessException(ErrorCode.MESSAGE_IMAGE_SIGNATURE_INVALID);
    }

    private int[] dimensions(byte[] bytes, String mime) throws IOException {
        if ("image/webp".equals(mime)) {
            return webpDimensions(bytes);
        }
        BufferedImage image = ImageIO.read(new ByteArrayInputStream(bytes));
        if (image == null) {
            throw new BusinessException(ErrorCode.MESSAGE_IMAGE_DECODE_INVALID);
        }
        return new int[]{image.getWidth(), image.getHeight()};
    }

    private int[] webpDimensions(byte[] bytes) {
        if (ascii(bytes, 12, "VP8X") && bytes.length >= 30) {
            return new int[]{1 + littleEndian24(bytes, 24), 1 + littleEndian24(bytes, 27)};
        }
        if (ascii(bytes, 12, "VP8L") && bytes.length >= 25 && unsigned(bytes[20]) == 0x2F) {
            int bits = unsigned(bytes[21]) | unsigned(bytes[22]) << 8 | unsigned(bytes[23]) << 16 | unsigned(bytes[24]) << 24;
            return new int[]{(bits & 0x3FFF) + 1, ((bits >> 14) & 0x3FFF) + 1};
        }
        if (ascii(bytes, 12, "VP8 ") && bytes.length >= 30
                && unsigned(bytes[23]) == 0x9D && unsigned(bytes[24]) == 0x01 && unsigned(bytes[25]) == 0x2A) {
            return new int[]{littleEndian16(bytes, 26) & 0x3FFF, littleEndian16(bytes, 28) & 0x3FFF};
        }
        throw new BusinessException(ErrorCode.MESSAGE_IMAGE_DECODE_INVALID);
    }

    private String sha256(byte[] bytes) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(bytes));
        } catch (Exception exception) {
            throw new IllegalStateException("SHA-256 không khả dụng", exception);
        }
    }

    private String extensionOf(String filename) {
        int dot = filename == null ? -1 : filename.lastIndexOf('.');
        return dot < 0 || dot == filename.length() - 1 ? "" : filename.substring(dot + 1).toLowerCase(Locale.ROOT);
    }

    private String normalizeDeclaredMime(String contentType) {
        if (contentType == null) return "";
        String normalized = contentType.trim().toLowerCase(Locale.ROOT);
        return "image/jpg".equals(normalized) ? "image/jpeg" : normalized;
    }

    private boolean ascii(byte[] bytes, int offset, String value) {
        if (offset < 0 || bytes.length < offset + value.length()) return false;
        for (int i = 0; i < value.length(); i++) if (bytes[offset + i] != (byte) value.charAt(i)) return false;
        return true;
    }

    private int unsigned(byte value) { return value & 0xFF; }
    private int littleEndian16(byte[] bytes, int offset) { return unsigned(bytes[offset]) | unsigned(bytes[offset + 1]) << 8; }
    private int littleEndian24(byte[] bytes, int offset) { return littleEndian16(bytes, offset) | unsigned(bytes[offset + 2]) << 16; }
}
