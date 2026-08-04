package com.stu.edu.vn.backend.post.dto.request;

import java.util.List;
import com.stu.edu.vn.backend.post.enums.LocationAction;
import org.springframework.web.multipart.MultipartFile;

/**
 * Request cập nhật bài viết dạng multipart, cho phép thay nội dung, hashtag và ảnh trong giới hạn nghiệp vụ.
 */
public record UpdatePostRequest(
        String content,
        String hashtag,
        List<Long> keepMediaIds,
        List<MultipartFile> newMediaFiles,
        LocationAction locationAction,
        PostLocationRequest location
) {
    public UpdatePostRequest(String content, String hashtag, List<Long> keepMediaIds,
                             List<MultipartFile> newMediaFiles) {
        this(content, hashtag, keepMediaIds, newMediaFiles, null, null);
    }
}
