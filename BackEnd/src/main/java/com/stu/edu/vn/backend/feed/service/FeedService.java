package com.stu.edu.vn.backend.feed.service;

import com.stu.edu.vn.backend.common.api.CursorPageResponse;
import com.stu.edu.vn.backend.feed.dto.FeedPostResponse;

public interface FeedService {
    CursorPageResponse<FeedPostResponse> getForYou(String cursor, int limit);

    CursorPageResponse<FeedPostResponse> getFollowing(String cursor, int limit);
}
