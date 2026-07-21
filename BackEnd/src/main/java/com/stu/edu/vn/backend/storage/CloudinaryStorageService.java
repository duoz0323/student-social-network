package com.stu.edu.vn.backend.storage;

import org.springframework.web.multipart.MultipartFile;

/**
 * Service lưu trữ ảnh trên Cloudinary để module avatar dùng ở giai đoạn tiếp theo.
 */
public interface CloudinaryStorageService {

    CloudinaryUploadResult uploadAvatar(MultipartFile file);

    void deleteImage(String publicId);
}
