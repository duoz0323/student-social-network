package com.stu.edu.vn.backend.post.dto.response;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Response chi tiet bai viet, khong tra Entity truc tiep va khong tra du lieu noi bo nhu status an/xoa.
 */
public record PostDetailResponse(
        Long id,
        String content,
        boolean isEdited,
        int likeCount,
        int commentCount,
        int repostCount,
        LocalDateTime publishedAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        PostAuthorResponse author,
        List<PostMediaResponse> media,
        String hashtag,
        PostViewerResponse viewer,
        boolean repostedByCurrentUser,
        PostLocationResponse location
) {
    public PostDetailResponse(Long id, String content, boolean isEdited, int likeCount, int commentCount,
                              LocalDateTime publishedAt, LocalDateTime createdAt, LocalDateTime updatedAt,
                              PostAuthorResponse author, List<PostMediaResponse> media, String hashtag,
                              PostViewerResponse viewer) {
        this(id, content, isEdited, likeCount, commentCount, 0, publishedAt, createdAt, updatedAt,
                author, media, hashtag, viewer, false, null);
    }
}
