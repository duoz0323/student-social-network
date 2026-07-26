package com.stu.edu.vn.backend.feed.dto;

import com.stu.edu.vn.backend.post.dto.response.PostAuthorResponse;
import com.stu.edu.vn.backend.post.dto.response.PostMediaResponse;
import java.time.LocalDateTime;
import java.util.List;

/** Dữ liệu PostCard trong Feed kèm trạng thái tương tác của người xem hiện tại. */
public record FeedPostResponse(
        Long postId,
        String content,
        boolean isEdited,
        int likeCount,
        int commentCount,
        LocalDateTime publishedAt,
        PostAuthorResponse author,
        List<PostMediaResponse> media,
        String hashtag,
        boolean likedByCurrentUser,
        boolean savedByCurrentUser
) {
}
