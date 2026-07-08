package com.stu.edu.vn.backend.post.validation;

import com.stu.edu.vn.backend.common.exception.BusinessException;
import com.stu.edu.vn.backend.common.exception.ErrorCode;
import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

/**
 * Kiểm tra ảnh bài viết tại Backend, không tin extension hoặc MIME type do Client gửi.
 */
@Component
public class PostImageFileValidator {

    private static final int MAX_IMAGE_COUNT = 4;
    private static final long MAX_IMAGE_SIZE_BYTES = 10L * 1024L * 1024L;
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("jpg", "jpeg", "png", "webp");
    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of("image/jpeg", "image/png", "image/webp");

    public void validate(List<MultipartFile> files) {
        // Không có ảnh là hợp lệ nếu bài viết có nội dung; PostValidationSupport kiểm tra điều kiện đó.
        if (files == null || files.isEmpty()) {
            return;
        }
        if (files.size() > MAX_IMAGE_COUNT) {
            throw new BusinessException(ErrorCode.POST_IMAGE_LIMIT_EXCEEDED);
        }
        files.forEach(this::validateOne);
    }

    public int countValidImageSlots(List<MultipartFile> files) {
        // Service dùng số lượng này để kiểm tra bài có ảnh mà không cần tự diễn giải null/list rỗng.
        return files == null ? 0 : files.size();
    }

    private void validateOne(MultipartFile file) {
        // Mỗi file được kiểm tra đủ size, extension, MIME type và chữ ký bytes.
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ErrorCode.POST_IMAGE_FILE_EMPTY);
        }
        if (file.getSize() > MAX_IMAGE_SIZE_BYTES) {
            throw new BusinessException(ErrorCode.POST_IMAGE_TOO_LARGE);
        }
        validateExtension(file.getOriginalFilename());
        validateContentType(file.getContentType());
        validateImageSignature(file);
    }

    private void validateExtension(String originalFilename) {
        // Extension chỉ là lớp kiểm tra đầu tiên, không thay thế kiểm tra MIME và chữ ký file.
        if (originalFilename == null) {
            throw new BusinessException(ErrorCode.POST_IMAGE_EXTENSION_NOT_ALLOWED);
        }
        int dotIndex = originalFilename.lastIndexOf('.');
        if (dotIndex < 0 || dotIndex == originalFilename.length() - 1) {
            throw new BusinessException(ErrorCode.POST_IMAGE_EXTENSION_NOT_ALLOWED);
        }
        String extension = originalFilename.substring(dotIndex + 1).toLowerCase(Locale.ROOT);
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new BusinessException(ErrorCode.POST_IMAGE_EXTENSION_NOT_ALLOWED);
        }
    }

    private void validateContentType(String contentType) {
        // MIME type từ multipart phải nằm trong danh sách ảnh mà MVP hỗ trợ.
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase(Locale.ROOT))) {
            throw new BusinessException(ErrorCode.POST_IMAGE_MIME_TYPE_INVALID);
        }
    }

    private void validateImageSignature(MultipartFile file) {
        // Đọc byte đầu để phát hiện file giả ảnh dù extension và MIME type hợp lệ.
        try {
            byte[] bytes = file.getBytes();
            if (!isJpeg(bytes) && !isPng(bytes) && !isWebp(bytes)) {
                throw new BusinessException(ErrorCode.POST_IMAGE_SIGNATURE_INVALID);
            }
        } catch (IOException exception) {
            throw new BusinessException(ErrorCode.POST_IMAGE_SIGNATURE_INVALID);
        }
    }

    private boolean isJpeg(byte[] bytes) {
        // JPEG bắt đầu bằng FF D8 FF.
        return bytes.length >= 3
                && (bytes[0] & 0xFF) == 0xFF
                && (bytes[1] & 0xFF) == 0xD8
                && (bytes[2] & 0xFF) == 0xFF;
    }

    private boolean isPng(byte[] bytes) {
        // PNG có signature cố định 8 byte đầu.
        return bytes.length >= 8
                && (bytes[0] & 0xFF) == 0x89
                && bytes[1] == 0x50
                && bytes[2] == 0x4E
                && bytes[3] == 0x47
                && bytes[4] == 0x0D
                && bytes[5] == 0x0A
                && bytes[6] == 0x1A
                && bytes[7] == 0x0A;
    }

    private boolean isWebp(byte[] bytes) {
        // WEBP nằm trong container RIFF và có marker WEBP tại byte 8-11.
        return bytes.length >= 12
                && bytes[0] == 0x52
                && bytes[1] == 0x49
                && bytes[2] == 0x46
                && bytes[3] == 0x46
                && bytes[8] == 0x57
                && bytes[9] == 0x45
                && bytes[10] == 0x42
                && bytes[11] == 0x50;
    }
}
