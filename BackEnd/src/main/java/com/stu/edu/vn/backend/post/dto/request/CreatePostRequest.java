package com.stu.edu.vn.backend.post.dto.request;

import java.util.List;
import org.springframework.web.multipart.MultipartFile;

/**
 * Request tạo bài viết dạng multipart, không chứa authorId vì tác giả lấy từ JWT.
 */
public record CreatePostRequest(
        String content,
        String hashtag,
        List<MultipartFile> mediaFiles,
        PostLocationRequest location
) {
    public CreatePostRequest(String content, String hashtag, List<MultipartFile> mediaFiles) {
        this(content, hashtag, mediaFiles, null);
    }
}
