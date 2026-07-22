package com.stu.edu.vn.backend.storage;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.stu.edu.vn.backend.common.exception.BusinessException;
import com.stu.edu.vn.backend.common.exception.ErrorCode;
import com.stu.edu.vn.backend.post.enums.PostMediaType;
import java.io.IOException;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/**
 * Adapter Cloudinary chịu trách nhiệm gọi SDK và che giấu lỗi kỹ thuật khỏi Client.
 */
@Service
@RequiredArgsConstructor
public class CloudinaryStorageServiceImpl implements CloudinaryStorageService {

    private static final String RESOURCE_TYPE_IMAGE = "image";
    private static final String RESOURCE_TYPE_VIDEO = "video";

    private final Cloudinary cloudinary;
    private final CloudinaryProperties properties;

    @Override
    public CloudinaryUploadResult uploadAvatar(MultipartFile file) {
        // Avatar chỉ cần URL và public_id; metadata chi tiết không trả ra API hồ sơ.
        return uploadImage(file, properties.getAvatarFolder(), ErrorCode.AVATAR_UPLOAD_FAILED, false);
    }

    @Override
    public CloudinaryUploadResult uploadPostImage(MultipartFile file) {
        // Ảnh bài viết cần metadata để lưu vào post_media sau khi tạo bài.
        return uploadImage(file, properties.getPostFolder(), ErrorCode.POST_IMAGE_UPLOAD_FAILED, true);
    }

    @Override
    public CloudinaryUploadResult uploadPostVideo(MultipartFile file) {
        // Video dùng resource_type riêng để Cloudinary xử lý metadata và thao tác xóa đúng loại tài nguyên.
        return uploadMedia(file, properties.getPostFolder(), RESOURCE_TYPE_VIDEO,
                ErrorCode.POST_VIDEO_UPLOAD_FAILED, true);
    }

    private CloudinaryUploadResult uploadImage(
            MultipartFile file,
            String folder,
            ErrorCode uploadErrorCode,
            boolean includeMetadata
    ) {
        return uploadMedia(file, folder, RESOURCE_TYPE_IMAGE, uploadErrorCode, includeMetadata);
    }

    private CloudinaryUploadResult uploadMedia(
            MultipartFile file,
            String folder,
            String resourceType,
            ErrorCode uploadErrorCode,
            boolean includeMetadata
    ) {
        ensureConfigured();
        try {
            Map<?, ?> uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.asMap(
                    "folder", folder,
                    "resource_type", resourceType
            ));
            if (!includeMetadata) {
                return new CloudinaryUploadResult(
                        stringValue(uploadResult.get("secure_url")),
                        stringValue(uploadResult.get("public_id"))
                );
            }
            return new CloudinaryUploadResult(
                    stringValue(uploadResult.get("secure_url")),
                    stringValue(uploadResult.get("public_id")),
                    toMimeType(resourceType, uploadResult.get("format")),
                    longValue(uploadResult.get("bytes")),
                    integerValue(uploadResult.get("width")),
                    integerValue(uploadResult.get("height")),
                    durationSeconds(uploadResult.get("duration")),
                    RESOURCE_TYPE_VIDEO.equals(resourceType)
                            ? videoThumbnail(stringValue(uploadResult.get("public_id")))
                            : null
            );
        } catch (IOException exception) {
            throw new BusinessException(uploadErrorCode);
        }
    }

    @Override
    public void deleteImage(String publicId) {
        if (publicId == null || publicId.trim().isEmpty()) {
            return;
        }
        ensureConfigured();
        try {
            cloudinary.uploader().destroy(publicId, ObjectUtils.asMap("resource_type", RESOURCE_TYPE_IMAGE));
        } catch (IOException exception) {
            throw new BusinessException(ErrorCode.AVATAR_DELETE_FAILED);
        }
    }

    @Override
    public void deletePostMedia(String publicId, PostMediaType mediaType) {
        if (publicId == null || publicId.trim().isEmpty()) {
            return;
        }
        ensureConfigured();
        String resourceType = mediaType == PostMediaType.VIDEO ? RESOURCE_TYPE_VIDEO : RESOURCE_TYPE_IMAGE;
        try {
            cloudinary.uploader().destroy(publicId, ObjectUtils.asMap("resource_type", resourceType));
        } catch (IOException exception) {
            throw new BusinessException(ErrorCode.POST_MEDIA_DELETE_FAILED);
        }
    }

    private void ensureConfigured() {
        if (!properties.isConfigured()) {
            throw new BusinessException(ErrorCode.CLOUDINARY_CONFIGURATION_INVALID);
        }
    }

    private String stringValue(Object value) {
        // Chỉ chuyển metadata cần thiết, không log hoặc trả credential Cloudinary.
        return value == null ? null : String.valueOf(value);
    }

    private Long longValue(Object value) {
        // Cloudinary có thể trả số ở nhiều kiểu Number tùy SDK/JSON parser.
        if (value instanceof Number number) {
            return number.longValue();
        }
        return value == null ? null : Long.valueOf(String.valueOf(value));
    }

    private Integer integerValue(Object value) {
        // Width/height có thể NULL nếu Cloudinary không trả metadata kích thước.
        if (value instanceof Number number) {
            return number.intValue();
        }
        return value == null ? null : Integer.valueOf(String.valueOf(value));
    }

    private String toMimeType(String resourceType, Object formatValue) {
        // Cloudinary trả format như jpg/png/webp/mp4/webm; database cần MIME type đầy đủ.
        String format = stringValue(formatValue);
        if (format == null || format.isBlank()) {
            return null;
        }
        String normalizedFormat = "jpg".equalsIgnoreCase(format) ? "jpeg" : format.toLowerCase(java.util.Locale.ROOT);
        return resourceType + "/" + normalizedFormat;
    }

    private Integer durationSeconds(Object value) {
        if (value == null) {
            return null;
        }
        double duration = value instanceof Number number
                ? number.doubleValue()
                : Double.parseDouble(String.valueOf(value));
        return (int) Math.ceil(duration);
    }

    private String videoThumbnail(String publicId) {
        return publicId == null ? null : cloudinary.url()
                .resourceType(RESOURCE_TYPE_VIDEO)
                .format("jpg")
                .generate(publicId);
    }
}
