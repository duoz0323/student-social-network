package com.stu.edu.vn.backend.post.validation;

import com.stu.edu.vn.backend.common.exception.BusinessException;
import com.stu.edu.vn.backend.common.exception.ErrorCode;
import com.stu.edu.vn.backend.post.enums.PostMediaType;
import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

/**
 * Kiểm tra media bài viết tại Backend, không tin riêng extension hoặc MIME type do Client gửi.
 */
@Component
public class PostImageFileValidator {

    private static final int MAX_IMAGE_COUNT = 4;
    private static final int MAX_VIDEO_COUNT = 1;
    private static final int MAX_MEDIA_COUNT = 4;
    private static final long MAX_IMAGE_SIZE_BYTES = 10L * 1024L * 1024L;
    private static final long MAX_VIDEO_SIZE_BYTES = 100L * 1024L * 1024L;
    private static final Set<String> IMAGE_EXTENSIONS = Set.of("jpg", "jpeg", "png", "webp");
    private static final Set<String> VIDEO_EXTENSIONS = Set.of("mp4", "webm");
    private static final Set<String> IMAGE_CONTENT_TYPES = Set.of("image/jpeg", "image/png", "image/webp");
    private static final Set<String> VIDEO_CONTENT_TYPES = Set.of("video/mp4", "video/webm");

    public void validate(List<MultipartFile> files) {
        if (files == null || files.isEmpty()) {
            return;
        }
        int imageCount = 0;
        int videoCount = 0;
        for (MultipartFile file : files) {
            if (file == null) {
                throw new BusinessException(ErrorCode.POST_IMAGE_FILE_EMPTY);
            }
            PostMediaType mediaType = detectMediaType(file);
            validateOne(file, mediaType);
            if (mediaType == PostMediaType.VIDEO) {
                videoCount++;
            } else {
                imageCount++;
            }
        }
        validateComposition(imageCount, videoCount);
    }

    public void validateComposition(int imageCount, int videoCount) {
        // Tổng media không vượt quá bốn; trong đó video luôn bị giới hạn ở một file.
        if (videoCount > MAX_VIDEO_COUNT) {
            throw new BusinessException(ErrorCode.POST_VIDEO_LIMIT_EXCEEDED);
        }
        if (imageCount > MAX_IMAGE_COUNT) {
            throw new BusinessException(ErrorCode.POST_IMAGE_LIMIT_EXCEEDED);
        }
        if (imageCount + videoCount > MAX_MEDIA_COUNT) {
            throw new BusinessException(ErrorCode.POST_MEDIA_LIMIT_EXCEEDED);
        }
    }

    public int countValidImageSlots(List<MultipartFile> files) {
        // Giữ method cũ cho các caller hiện tại; giá trị là tổng số media đã hợp lệ.
        return files == null ? 0 : files.size();
    }

    public PostMediaType detectMediaType(MultipartFile file) {
        if (file == null) {
            throw new BusinessException(ErrorCode.POST_IMAGE_FILE_EMPTY);
        }
        String extension = extensionOf(file.getOriginalFilename());
        if (VIDEO_EXTENSIONS.contains(extension)) {
            return PostMediaType.VIDEO;
        }
        if (IMAGE_EXTENSIONS.contains(extension)) {
            return PostMediaType.IMAGE;
        }
        throw new BusinessException(ErrorCode.POST_IMAGE_EXTENSION_NOT_ALLOWED);
    }

    private void validateOne(MultipartFile file, PostMediaType mediaType) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(mediaType == PostMediaType.VIDEO
                    ? ErrorCode.POST_VIDEO_FILE_EMPTY
                    : ErrorCode.POST_IMAGE_FILE_EMPTY);
        }
        validateSize(file, mediaType);
        validateContentType(file, mediaType);
        validateSignature(file, mediaType);
    }

    private void validateSize(MultipartFile file, PostMediaType mediaType) {
        long maxSize = mediaType == PostMediaType.VIDEO ? MAX_VIDEO_SIZE_BYTES : MAX_IMAGE_SIZE_BYTES;
        if (file.getSize() > maxSize) {
            throw new BusinessException(mediaType == PostMediaType.VIDEO
                    ? ErrorCode.POST_VIDEO_TOO_LARGE
                    : ErrorCode.POST_IMAGE_TOO_LARGE);
        }
    }

    private void validateContentType(MultipartFile file, PostMediaType mediaType) {
        String contentType = file.getContentType() == null
                ? null
                : file.getContentType().toLowerCase(Locale.ROOT);
        boolean valid = mediaType == PostMediaType.VIDEO
                ? VIDEO_CONTENT_TYPES.contains(contentType)
                : IMAGE_CONTENT_TYPES.contains(contentType);
        if (!valid) {
            throw new BusinessException(mediaType == PostMediaType.VIDEO
                    ? ErrorCode.POST_VIDEO_MIME_TYPE_INVALID
                    : ErrorCode.POST_IMAGE_MIME_TYPE_INVALID);
        }
    }

    private void validateSignature(MultipartFile file, PostMediaType mediaType) {
        try {
            byte[] bytes = file.getBytes();
            boolean valid = mediaType == PostMediaType.VIDEO
                    ? isMp4(bytes) || isWebm(bytes)
                    : isJpeg(bytes) || isPng(bytes) || isWebp(bytes);
            if (!valid) {
                throw invalidSignature(mediaType);
            }
        } catch (IOException exception) {
            throw invalidSignature(mediaType);
        }
    }

    private BusinessException invalidSignature(PostMediaType mediaType) {
        return new BusinessException(mediaType == PostMediaType.VIDEO
                ? ErrorCode.POST_VIDEO_SIGNATURE_INVALID
                : ErrorCode.POST_IMAGE_SIGNATURE_INVALID);
    }

    private String extensionOf(String filename) {
        if (filename == null) {
            return "";
        }
        int dotIndex = filename.lastIndexOf('.');
        if (dotIndex < 0 || dotIndex == filename.length() - 1) {
            return "";
        }
        return filename.substring(dotIndex + 1).toLowerCase(Locale.ROOT);
    }

    private boolean isJpeg(byte[] bytes) {
        return bytes.length >= 3 && (bytes[0] & 0xFF) == 0xFF
                && (bytes[1] & 0xFF) == 0xD8 && (bytes[2] & 0xFF) == 0xFF;
    }

    private boolean isPng(byte[] bytes) {
        return bytes.length >= 8 && (bytes[0] & 0xFF) == 0x89 && bytes[1] == 0x50
                && bytes[2] == 0x4E && bytes[3] == 0x47 && bytes[4] == 0x0D
                && bytes[5] == 0x0A && bytes[6] == 0x1A && bytes[7] == 0x0A;
    }

    private boolean isWebp(byte[] bytes) {
        return bytes.length >= 12 && bytes[0] == 0x52 && bytes[1] == 0x49
                && bytes[2] == 0x46 && bytes[3] == 0x46 && bytes[8] == 0x57
                && bytes[9] == 0x45 && bytes[10] == 0x42 && bytes[11] == 0x50;
    }

    private boolean isMp4(byte[] bytes) {
        // ISO Base Media File Format khai báo box ftyp tại byte 4-7.
        return bytes.length >= 12 && bytes[4] == 0x66 && bytes[5] == 0x74
                && bytes[6] == 0x79 && bytes[7] == 0x70;
    }

    private boolean isWebm(byte[] bytes) {
        // WebM dùng EBML header 1A 45 DF A3.
        return bytes.length >= 4 && (bytes[0] & 0xFF) == 0x1A && bytes[1] == 0x45
                && (bytes[2] & 0xFF) == 0xDF && (bytes[3] & 0xFF) == 0xA3;
    }
}
