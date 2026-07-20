package com.stu.edu.vn.backend.storage;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.stu.edu.vn.backend.common.exception.BusinessException;
import com.stu.edu.vn.backend.common.exception.ErrorCode;
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

    private final Cloudinary cloudinary;
    private final CloudinaryProperties properties;

    @Override
    public CloudinaryUploadResult uploadAvatar(MultipartFile file) {
        ensureConfigured();
        try {
            Map<?, ?> uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.asMap(
                    "folder", properties.getAvatarFolder(),
                    "resource_type", RESOURCE_TYPE_IMAGE
            ));
            return new CloudinaryUploadResult(
                    String.valueOf(uploadResult.get("secure_url")),
                    String.valueOf(uploadResult.get("public_id"))
            );
        } catch (IOException exception) {
            throw new BusinessException(ErrorCode.AVATAR_UPLOAD_FAILED);
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

    private void ensureConfigured() {
        if (!properties.isConfigured()) {
            throw new BusinessException(ErrorCode.CLOUDINARY_CONFIGURATION_INVALID);
        }
    }
}
