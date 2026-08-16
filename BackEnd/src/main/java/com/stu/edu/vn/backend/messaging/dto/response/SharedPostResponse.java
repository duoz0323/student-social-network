package com.stu.edu.vn.backend.messaging.dto.response;

import com.stu.edu.vn.backend.post.dto.response.PostMediaResponse;
import java.util.List;

/** Projection Post nhỏ cho Message, không sao chép dữ liệu Post vào bảng messages. */
public record SharedPostResponse(Long postId, SharedPostAuthorResponse author, String content,
                                 List<PostMediaResponse> media, int likeCount,
                                 int commentCount, int repostCount) { }
