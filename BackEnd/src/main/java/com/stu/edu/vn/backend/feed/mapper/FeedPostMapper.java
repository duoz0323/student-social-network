package com.stu.edu.vn.backend.feed.mapper;

import com.stu.edu.vn.backend.feed.dto.FeedPostResponse;
import com.stu.edu.vn.backend.post.dto.response.PostAuthorResponse;
import com.stu.edu.vn.backend.post.dto.response.PostMediaResponse;
import com.stu.edu.vn.backend.post.dto.response.PostLocationResponse;
import com.stu.edu.vn.backend.location.entity.Location;
import com.stu.edu.vn.backend.post.entity.Post;
import com.stu.edu.vn.backend.post.entity.PostMedia;
import com.stu.edu.vn.backend.user.entity.UserProfile;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

/** Mapper Feed chỉ nhận dữ liệu đã batch-load, không truy cập Repository. */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface FeedPostMapper {

    @Mapping(target = "postId", source = "post.id")
    @Mapping(target = "content", source = "post.content")
    @Mapping(target = "isEdited", source = "post.edited")
    @Mapping(target = "likeCount", source = "post.likeCount")
    @Mapping(target = "commentCount", source = "post.commentCount")
    @Mapping(target = "repostCount", source = "post.repostCount")
    @Mapping(target = "publishedAt", source = "post.publishedAt")
    @Mapping(target = "author", source = "profile")
    @Mapping(target = "media", source = "media")
    @Mapping(target = "hashtag", source = "hashtag")
    @Mapping(target = "likedByCurrentUser", source = "liked")
    @Mapping(target = "savedByCurrentUser", source = "saved")
    @Mapping(target = "repostedByCurrentUser", source = "reposted")
    @Mapping(target = "location", source = "location")
    FeedPostResponse toResponse(Post post, UserProfile profile, List<PostMedia> media,
                                String hashtag, boolean liked, boolean saved, boolean reposted, Location location);

    default FeedPostResponse toResponse(Post post, UserProfile profile, List<PostMedia> media,
                                        String hashtag, boolean liked, boolean saved) {
        return toResponse(post, profile, media, hashtag, liked, saved, false, null);
    }

    default FeedPostResponse toResponse(Post post, UserProfile profile, List<PostMedia> media,
                                        String hashtag, boolean liked, boolean saved, Location location) {
        return toResponse(post, profile, media, hashtag, liked, saved, false, location);
    }

    @Mapping(target = "id", source = "userId")
    PostAuthorResponse toAuthor(UserProfile profile);

    @Mapping(target = "url", source = "mediaUrl")
    @Mapping(target = "fileSize", source = "fileSizeBytes")
    @Mapping(target = "width", source = "widthPx")
    @Mapping(target = "height", source = "heightPx")
    PostMediaResponse toMedia(PostMedia media);

    @Mapping(target = "placeId", source = "googlePlaceId")
    PostLocationResponse toLocation(Location location);
}
