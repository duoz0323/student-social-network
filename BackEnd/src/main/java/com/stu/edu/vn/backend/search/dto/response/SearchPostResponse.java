package com.stu.edu.vn.backend.search.dto.response;

import com.stu.edu.vn.backend.post.dto.response.PostAuthorResponse;
import com.stu.edu.vn.backend.post.dto.response.PostMediaResponse;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Dữ liệu Post Card công khai kèm trạng thái tương tác của người dùng hiện tại.
 */
public record SearchPostResponse(
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
