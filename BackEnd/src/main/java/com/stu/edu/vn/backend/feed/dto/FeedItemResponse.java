package com.stu.edu.vn.backend.feed.dto;

import com.stu.edu.vn.backend.feed.enums.FeedItemType;
import com.stu.edu.vn.backend.post.dto.response.PostAuthorResponse;
import java.time.LocalDateTime;

/** Một hoạt động trong Following/Profile Repost, luôn chứa projection an toàn của bài gốc. */
public record FeedItemResponse(
        FeedItemType itemType,
        LocalDateTime activityAt,
        LocalDateTime repostedAt,
        PostAuthorResponse repostedBy,
        FeedPostResponse post
) {
}
