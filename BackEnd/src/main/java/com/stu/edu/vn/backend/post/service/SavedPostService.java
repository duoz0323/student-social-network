package com.stu.edu.vn.backend.post.service;

import com.stu.edu.vn.backend.common.api.CursorPageResponse;
import com.stu.edu.vn.backend.feed.dto.FeedPostResponse;
import com.stu.edu.vn.backend.post.dto.response.PostSaveResponse;

/**
 * Service quản lý Save/Unsave bài viết theo người dùng hiện tại trong SecurityContext.
 */
public interface SavedPostService {

    PostSaveResponse savePost(Long postId);

    PostSaveResponse unsavePost(Long postId);

    CursorPageResponse<FeedPostResponse> getSavedPosts(String cursor, int limit);


}
