package com.stu.edu.vn.backend.post.mapper;

import com.stu.edu.vn.backend.post.dto.response.PostAuthorResponse;
import com.stu.edu.vn.backend.post.dto.response.PostDetailResponse;
import com.stu.edu.vn.backend.post.dto.response.PostMediaResponse;
import com.stu.edu.vn.backend.post.dto.response.PostResponse;
import com.stu.edu.vn.backend.post.dto.response.PostViewerResponse;
import com.stu.edu.vn.backend.post.entity.Post;
import com.stu.edu.vn.backend.post.entity.PostMedia;
import com.stu.edu.vn.backend.user.entity.UserProfile;
import java.util.List;
import org.springframework.stereotype.Component;

/**
 * Mapper chuyển Entity bài viết sang DTO để Controller không trả Entity trực tiếp.
 */
@Component
public class PostMapper {

    public PostResponse toResponse(
            Post post,
            UserProfile authorProfile,
            List<PostMedia> media,
            String hashtag
    ) {
        // Response chỉ gồm dữ liệu công khai, không chứa publicId media hoặc dữ liệu xác thực.
        return new PostResponse(
                post.getId(),
                post.getContent(),
                post.getStatus(),
                post.isEdited(),
                post.getLikeCount(),
                post.getCommentCount(),
                post.getPublishedAt(),
                post.getCreatedAt(),
                post.getUpdatedAt(),
                toAuthorResponse(authorProfile),
                media.stream().map(this::toMediaResponse).toList(),
                hashtag
        );
    }

    public PostDetailResponse toDetailResponse(
            Post post,
            UserProfile authorProfile,
            List<PostMedia> media,
            String hashtag,
            boolean owner
    ) {
        // Response chi tiết chỉ dùng cho bài PUBLISHED đã qua kiểm tra ở Service, nên không trả status ẩn/xóa ra API công khai.
        return new PostDetailResponse(
                post.getId(),
                post.getContent(),
                post.isEdited(),
                post.getLikeCount(),
                post.getCommentCount(),
                post.getPublishedAt(),
                post.getCreatedAt(),
                post.getUpdatedAt(),
                toAuthorResponse(authorProfile),
                media.stream().map(this::toMediaResponse).toList(),
                hashtag,
                new PostViewerResponse(owner)
        );
    }

    private PostAuthorResponse toAuthorResponse(UserProfile authorProfile) {
        return new PostAuthorResponse(
                authorProfile.getUserId(),
                authorProfile.getDisplayName(),
                authorProfile.getAvatarUrl()
        );
    }

    private PostMediaResponse toMediaResponse(PostMedia media) {
        return new PostMediaResponse(
                media.getId(),
                media.getMediaUrl(),
                media.getMediaType(),
                media.getMimeType(),
                media.getFileSizeBytes(),
                media.getWidthPx(),
                media.getHeightPx(),
                media.getDurationSeconds(),
                media.getThumbnailUrl(),
                media.getDisplayOrder()
        );
    }
}
