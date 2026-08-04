package com.stu.edu.vn.backend.search.mapper;

import com.stu.edu.vn.backend.post.dto.response.PostAuthorResponse;
import com.stu.edu.vn.backend.post.dto.response.PostMediaResponse;
import com.stu.edu.vn.backend.post.dto.response.PostLocationResponse;
import com.stu.edu.vn.backend.location.entity.Location;
import com.stu.edu.vn.backend.post.entity.Post;
import com.stu.edu.vn.backend.post.entity.PostMedia;
import com.stu.edu.vn.backend.search.dto.response.SearchPostResponse;
import com.stu.edu.vn.backend.user.entity.UserProfile;
import java.util.List;
import org.springframework.stereotype.Component;

/**
 * Mapper Post Card chỉ nhận dữ liệu đã batch-load; mapper không truy cập Repository và không gây N+1.
 */
@Component
public class SearchPostMapper {

    public SearchPostResponse toResponse(Post post, UserProfile authorProfile, List<PostMedia> media,
                                         String hashtag, boolean liked, boolean saved, boolean reposted,
                                         Location location) {
        PostAuthorResponse author = new PostAuthorResponse(
                authorProfile.getUserId(), authorProfile.getDisplayName(), authorProfile.getAvatarUrl());
        List<PostMediaResponse> mediaResponses = media.stream().map(this::toMediaResponse).toList();
        return new SearchPostResponse(
                post.getId(), post.getContent(), post.isEdited(), post.getLikeCount(), post.getCommentCount(),
                post.getRepostCount(), post.getPublishedAt(), author, mediaResponses, hashtag,
                liked, saved, reposted, toLocation(location)
        );
    }

    public SearchPostResponse toResponse(Post post, UserProfile authorProfile, List<PostMedia> media,
                                         String hashtag, boolean liked, boolean saved) {
        return toResponse(post, authorProfile, media, hashtag, liked, saved, false, null);
    }

    public SearchPostResponse toResponse(Post post, UserProfile authorProfile, List<PostMedia> media,
                                         String hashtag, boolean liked, boolean saved, Location location) {
        return toResponse(post, authorProfile, media, hashtag, liked, saved, false, location);
    }

    private PostLocationResponse toLocation(Location location) {
        return location == null ? null : new PostLocationResponse(location.getId(), location.getGooglePlaceId(),
                location.getDisplayName(), location.getFormattedAddress(),
                location.getLatitude(), location.getLongitude());
    }

    private PostMediaResponse toMediaResponse(PostMedia media) {
        // Không ánh xạ storagePublicId vì đây là khóa quản trị nội bộ của dịch vụ lưu trữ.
        return new PostMediaResponse(
                media.getId(), media.getMediaUrl(), media.getMediaType(), media.getMimeType(),
                media.getFileSizeBytes(), media.getWidthPx(), media.getHeightPx(),
                media.getDurationSeconds(), media.getThumbnailUrl(), media.getDisplayOrder()
        );
    }
}
