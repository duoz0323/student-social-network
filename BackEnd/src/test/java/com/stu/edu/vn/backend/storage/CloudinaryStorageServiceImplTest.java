package com.stu.edu.vn.backend.storage;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.cloudinary.Cloudinary;
import com.cloudinary.Uploader;
import com.cloudinary.Url;
import com.stu.edu.vn.backend.common.exception.BusinessException;
import com.stu.edu.vn.backend.common.exception.ErrorCode;
import java.io.IOException;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

class CloudinaryStorageServiceImplTest {

    private final Cloudinary cloudinary = org.mockito.Mockito.mock(Cloudinary.class);
    private final Uploader uploader = org.mockito.Mockito.mock(Uploader.class);
    private final Url cloudinaryUrl = org.mockito.Mockito.mock(Url.class);
    private final CloudinaryProperties properties = new CloudinaryProperties();

    private CloudinaryStorageServiceImpl storageService;

    @BeforeEach
    void setUp() {
        // Cấu hình giả lập chỉ dùng trong unit test, không chứa credential thật.
        properties.setCloudName("demo-cloud");
        properties.setApiKey("demo-key");
        properties.setApiSecret("demo-secret");
        properties.setPostFolder("student-social-network/posts");
        storageService = new CloudinaryStorageServiceImpl(cloudinary, properties);
        when(cloudinary.uploader()).thenReturn(uploader);
        when(cloudinary.url()).thenReturn(cloudinaryUrl);
        when(cloudinaryUrl.resourceType("video")).thenReturn(cloudinaryUrl);
        when(cloudinaryUrl.format("jpg")).thenReturn(cloudinaryUrl);
    }

    @Test
    void uploadPostImageReturnsMetadataFromCloudinaryResponse() throws Exception {
        // Ảnh bài viết cần URL, publicId, MIME type, size và kích thước để lưu post_media.
        MockMultipartFile file = new MockMultipartFile("images", "post.jpg", "image/jpeg", new byte[]{1, 2, 3});
        when(uploader.upload(any(byte[].class), any(Map.class))).thenReturn(Map.of(
                "secure_url", "https://cdn.example/post.jpg",
                "public_id", "student-social-network/posts/post-id",
                "format", "jpg",
                "bytes", 1234,
                "width", 640,
                "height", 480
        ));

        CloudinaryUploadResult result = storageService.uploadPostImage(file);

        assertThat(result.url()).isEqualTo("https://cdn.example/post.jpg");
        assertThat(result.publicId()).isEqualTo("student-social-network/posts/post-id");
        assertThat(result.mimeType()).isEqualTo("image/jpeg");
        assertThat(result.fileSize()).isEqualTo(1234L);
        assertThat(result.width()).isEqualTo(640);
        assertThat(result.height()).isEqualTo(480);
    }

    @Test
    void uploadPostImageThrowsBusinessExceptionWhenCloudinaryFails() throws Exception {
        // Lỗi hạ tầng Cloudinary được che giấu bằng mã lỗi nghiệp vụ của ảnh bài viết.
        MockMultipartFile file = new MockMultipartFile("images", "post.png", "image/png", new byte[]{1});
        when(uploader.upload(any(byte[].class), any(Map.class))).thenThrow(new IOException("upload failed"));

        assertThatThrownBy(() -> storageService.uploadPostImage(file))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.POST_IMAGE_UPLOAD_FAILED);
    }

    @Test
    void uploadPostVideoReturnsDurationAndGeneratedThumbnail() throws Exception {
        MockMultipartFile file = new MockMultipartFile("mediaFiles", "intro.mp4", "video/mp4", new byte[]{1});
        when(uploader.upload(any(byte[].class), any(Map.class))).thenReturn(Map.of(
                "secure_url", "https://cdn.example/intro.mp4",
                "public_id", "student-social-network/posts/intro",
                "format", "mp4",
                "bytes", 4096,
                "width", 1280,
                "height", 720,
                "duration", 45.2
        ));
        when(cloudinaryUrl.generate("student-social-network/posts/intro"))
                .thenReturn("https://cdn.example/intro.jpg");

        CloudinaryUploadResult result = storageService.uploadPostVideo(file);

        assertThat(result.mimeType()).isEqualTo("video/mp4");
        assertThat(result.durationSeconds()).isEqualTo(46);
        assertThat(result.thumbnailUrl()).isEqualTo("https://cdn.example/intro.jpg");
    }

    @Test
    void uploadAvatarStillReturnsUrlAndPublicId() throws Exception {
        // Luồng avatar cũ vẫn hoạt động với constructor rút gọn của CloudinaryUploadResult.
        MockMultipartFile file = new MockMultipartFile("file", "avatar.png", "image/png", new byte[]{1});
        when(uploader.upload(any(byte[].class), any(Map.class))).thenReturn(Map.of(
                "secure_url", "https://cdn.example/avatar.png",
                "public_id", "student-social-network/avatars/avatar-id"
        ));

        CloudinaryUploadResult result = storageService.uploadAvatar(file);

        assertThat(result.url()).isEqualTo("https://cdn.example/avatar.png");
        assertThat(result.publicId()).isEqualTo("student-social-network/avatars/avatar-id");
        assertThat(result.mimeType()).isNull();
    }
}
