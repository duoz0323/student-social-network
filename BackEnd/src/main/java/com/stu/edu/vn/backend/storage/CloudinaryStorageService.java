package com.stu.edu.vn.backend.storage;

import com.stu.edu.vn.backend.post.enums.PostMediaType;
import org.springframework.web.multipart.MultipartFile;

/**
 * Service lưu trữ ảnh trên Cloudinary để module avatar dùng ở giai đoạn tiếp theo.
 */
public interface CloudinaryStorageService {

    CloudinaryUploadResult uploadAvatar(MultipartFile file);

    CloudinaryUploadResult uploadPostImage(MultipartFile file);

    CloudinaryUploadResult uploadPostVideo(MultipartFile file);
    CloudinaryUploadResult uploadMessageImage(MultipartFile file);
    CloudinaryAccessResult createMessageImageAccess(String publicId, String mimeType);

    void deleteImage(String publicId);

    void deletePostMedia(String publicId, PostMediaType mediaType);
    void deleteMessageImage(String publicId);
}
