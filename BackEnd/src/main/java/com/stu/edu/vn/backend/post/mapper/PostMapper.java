package com.stu.edu.vn.backend.post.mapper;

import com.stu.edu.vn.backend.post.dto.response.PostAuthorResponse;
import com.stu.edu.vn.backend.post.dto.response.PostDetailResponse;
import com.stu.edu.vn.backend.post.dto.response.PostMediaResponse;
import com.stu.edu.vn.backend.post.dto.response.PostLocationResponse;
import com.stu.edu.vn.backend.post.dto.response.PostResponse;
import com.stu.edu.vn.backend.post.dto.response.PostViewerResponse;
import com.stu.edu.vn.backend.post.entity.Post;
import com.stu.edu.vn.backend.post.entity.PostMedia;
import com.stu.edu.vn.backend.location.entity.Location;
import com.stu.edu.vn.backend.user.entity.UserProfile;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

/**
 * MapStruct mapper chuyển Entity bài viết sang DTO và chỉ công khai các trường an toàn.
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface PostMapper {

    @Mapping(target = "id", source = "post.id")
    @Mapping(target = "content", source = "post.content")
    @Mapping(target = "status", source = "post.status")
    @Mapping(target = "isEdited", source = "post.edited")
    @Mapping(target = "likeCount", source = "post.likeCount")
    @Mapping(target = "commentCount", source = "post.commentCount")
    @Mapping(target = "repostCount", source = "post.repostCount")
    @Mapping(target = "publishedAt", source = "post.publishedAt")
    @Mapping(target = "createdAt", source = "post.createdAt")
    @Mapping(target = "updatedAt", source = "post.updatedAt")
    @Mapping(target = "author", source = "authorProfile")
    @Mapping(target = "media", source = "media")
    @Mapping(target = "hashtag", source = "hashtag")
    @Mapping(target = "location", source = "post.location")
    @Mapping(target = "repostedByCurrentUser", constant = "false")
    PostResponse toResponse(
            Post post,
            UserProfile authorProfile,
            List<PostMedia> media,
            String hashtag
    );

    @Mapping(target = "id", source = "post.id")
    @Mapping(target = "content", source = "post.content")
    @Mapping(target = "isEdited", source = "post.edited")
    @Mapping(target = "likeCount", source = "post.likeCount")
    @Mapping(target = "commentCount", source = "post.commentCount")
    @Mapping(target = "repostCount", source = "post.repostCount")
    @Mapping(target = "publishedAt", source = "post.publishedAt")
    @Mapping(target = "createdAt", source = "post.createdAt")
    @Mapping(target = "updatedAt", source = "post.updatedAt")
    @Mapping(target = "author", source = "authorProfile")
    @Mapping(target = "media", source = "media")
    @Mapping(target = "hashtag", source = "hashtag")
    @Mapping(target = "viewer", source = "owner")
    @Mapping(target = "location", source = "post.location")
    @Mapping(target = "repostedByCurrentUser", source = "reposted")
    PostDetailResponse toDetailResponse(
            Post post,
            UserProfile authorProfile,
            List<PostMedia> media,
            String hashtag,
            boolean owner,
            boolean reposted
    );

    default PostDetailResponse toDetailResponse(Post post, UserProfile authorProfile, List<PostMedia> media,
                                                String hashtag, boolean owner) {
        return toDetailResponse(post, authorProfile, media, hashtag, owner, false);
    }

    @Mapping(target = "id", source = "userId")
    PostAuthorResponse toAuthorResponse(UserProfile authorProfile);

    @Mapping(target = "url", source = "mediaUrl")
    @Mapping(target = "fileSize", source = "fileSizeBytes")
    @Mapping(target = "width", source = "widthPx")
    @Mapping(target = "height", source = "heightPx")
    PostMediaResponse toMediaResponse(PostMedia media);

    @Mapping(target = "placeId", source = "googlePlaceId")
    PostLocationResponse toLocationResponse(Location location);

    default PostViewerResponse toViewerResponse(boolean owner) {
        return new PostViewerResponse(owner);
    }
}
