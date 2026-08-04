package com.stu.edu.vn.backend.post.service;

import com.stu.edu.vn.backend.common.api.CursorPageResponse;
import com.stu.edu.vn.backend.feed.dto.FeedItemResponse;
import com.stu.edu.vn.backend.post.dto.response.PostRepostResponse;

/** Contract thao tác Repost và đọc tab Đăng lại trên Profile. */
public interface PostRepostService {
    PostRepostResponse repost(Long postId);

    PostRepostResponse unrepost(Long postId);

    CursorPageResponse<FeedItemResponse> getProfileReposts(Long userId, String cursor, int limit);
}
