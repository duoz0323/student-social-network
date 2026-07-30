package com.stu.edu.vn.backend.admin.mapper;

import com.stu.edu.vn.backend.admin.dto.response.AdminPostAuthorResponse;
import com.stu.edu.vn.backend.admin.dto.response.AdminPostDetailResponse;
import com.stu.edu.vn.backend.admin.dto.response.AdminPostHiddenByResponse;
import com.stu.edu.vn.backend.admin.dto.response.AdminPostListItemResponse;
import com.stu.edu.vn.backend.admin.dto.response.AdminPostMediaResponse;
import com.stu.edu.vn.backend.admin.dto.response.AdminPostStatusResponse;
import com.stu.edu.vn.backend.admin.repository.projection.AdminPostDetailProjection;
import com.stu.edu.vn.backend.admin.repository.projection.AdminPostListProjection;
import com.stu.edu.vn.backend.admin.repository.projection.AdminPostMediaProjection;
import com.stu.edu.vn.backend.post.entity.Post;
import com.stu.edu.vn.backend.post.enums.PostMediaType;
import com.stu.edu.vn.backend.post.enums.PostStatus;
import com.stu.edu.vn.backend.post.dto.response.PostLocationResponse;
import com.stu.edu.vn.backend.user.enums.UserStatus;
import java.util.List;
import org.springframework.stereotype.Component;

/** Chuyển projection quản trị sang response DTO ổn định. */
@Component
public class AdminPostMapper {

    public AdminPostStatusResponse toStatus(Post post, Long adminId, String adminDisplayName) {
        AdminPostHiddenByResponse hiddenBy = post.getStatus() == PostStatus.HIDDEN
                ? new AdminPostHiddenByResponse(adminId, adminDisplayName)
                : null;
        return new AdminPostStatusResponse(post.getId(), post.getStatus(), post.getHiddenAt(),
                post.getHiddenReason(), hiddenBy, post.getUpdatedAt());
    }

    public AdminPostListItemResponse toListItem(AdminPostListProjection source) {
        return new AdminPostListItemResponse(source.getPostId(), source.getContentPreview(),
                PostStatus.valueOf(source.getStatus()), source.getAuthorId(), source.getAuthorDisplayName(),
                source.getAuthorAvatarUrl(), UserStatus.valueOf(source.getAuthorAccountStatus()),
                source.getThumbnailUrl(), source.getMediaCount(), source.getLikeCount(), source.getCommentCount(),
                source.getPendingReportCount(), source.getCreatedAt(), source.getUpdatedAt());
    }

    public AdminPostDetailResponse toDetail(AdminPostDetailProjection source,
            List<AdminPostMediaProjection> media, String hashtag) {
        AdminPostAuthorResponse author = new AdminPostAuthorResponse(source.getAuthorId(),
                source.getAuthorDisplayName(), source.getAuthorAvatarUrl(), source.getAuthorEmail(),
                UserStatus.valueOf(source.getAuthorAccountStatus()));
        AdminPostHiddenByResponse hiddenBy = source.getHiddenByAdminId() == null ? null
                : new AdminPostHiddenByResponse(source.getHiddenByAdminId(), source.getHiddenByDisplayName());

        // Tạo danh sách DTO mới để response không giữ projection do persistence provider quản lý.
        List<AdminPostMediaResponse> mediaResponses = media.stream()
                .map(item -> new AdminPostMediaResponse(item.getMediaId(), item.getMediaUrl(),
                        item.getMediaType() == null ? PostMediaType.IMAGE
                                : PostMediaType.valueOf(item.getMediaType()),
                        item.getMimeType(), item.getDurationSeconds(),
                        item.getThumbnailUrl(), item.getSortOrder()))
                .toList();
        PostLocationResponse location = source.getLocationId() == null ? null : new PostLocationResponse(
                source.getLocationId(), source.getPlaceId(), source.getLocationDisplayName(),
                source.getLocationFormattedAddress(), source.getLocationLatitude(), source.getLocationLongitude());
        return new AdminPostDetailResponse(source.getPostId(), source.getContent(),
                PostStatus.valueOf(source.getStatus()), author, mediaResponses, hashtag,
                source.getLikeCount(), source.getCommentCount(), source.getPendingReportCount(),
                source.getTotalReportCount(), source.getHiddenAt(), source.getHiddenReason(), hiddenBy,
                source.getDeletedAt(), source.getCreatedAt(), source.getUpdatedAt(), location);
    }
}
