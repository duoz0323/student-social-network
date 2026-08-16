package com.stu.edu.vn.backend.post.dto.response;

import com.stu.edu.vn.backend.post.enums.PostStatus;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Chi tiết quản lý dành riêng cho tác giả, gồm cả trạng thái ẩn/xóa mà API công khai không tiết lộ.
 */
public record OwnedPostDetailResponse(
        Long id,
        String content,
        boolean isEdited,
        int likeCount,
        int commentCount,
        int repostCount,
        PostStatus status,
        LocalDateTime publishedAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        LocalDateTime hiddenAt,
        String hiddenReason,
        LocalDateTime deletedAt,
        PostAuthorResponse author,
        List<PostMediaResponse> media,
        String hashtag,
        PostViewerResponse viewer,
        boolean repostedByCurrentUser,
        PostLocationResponse location
) {
    public static OwnedPostDetailResponse from(PostDetailResponse detail, PostStatus status,
                                                LocalDateTime hiddenAt, String hiddenReason,
                                                LocalDateTime deletedAt) {
        return new OwnedPostDetailResponse(
                detail.id(), detail.content(), detail.isEdited(), detail.likeCount(), detail.commentCount(),
                detail.repostCount(), status, detail.publishedAt(), detail.createdAt(), detail.updatedAt(),
                hiddenAt, hiddenReason, deletedAt, detail.author(), detail.media(), detail.hashtag(),
                detail.viewer(), detail.repostedByCurrentUser(), detail.location()
        );
    }
}
