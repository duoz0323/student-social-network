package com.stu.edu.vn.backend.interaction.mapper;

import com.stu.edu.vn.backend.interaction.dto.response.CommentResponse;
import com.stu.edu.vn.backend.interaction.entity.Comment;
import com.stu.edu.vn.backend.interaction.enums.CommentStatus;
import com.stu.edu.vn.backend.user.entity.UserProfile;
import org.springframework.stereotype.Component;

/**
 * Mapper chuyển Comment entity sang DTO để API không trả entity trực tiếp.
 */
@Component
public class CommentMapper {

    public CommentResponse toResponse(Comment comment, long replyCount) {
        boolean deleted = comment.getStatus() == CommentStatus.DELETED;
        UserProfile profile = comment.getAuthorProfile();
        return new CommentResponse(
                comment.getId(),
                comment.getPost().getId(),
                comment.getParentComment() == null ? null : comment.getParentComment().getId(),
                deleted ? null : comment.getAuthor().getId(),
                deleted || profile == null ? null : profile.getDisplayName(),
                deleted || profile == null ? null : profile.getAvatarUrl(),
                deleted ? null : comment.getContent(),
                comment.getCreatedAt(),
                replyCount,
                deleted
        );
    }

    public CommentResponse toResponse(Comment comment) {
        // Reply không có cấp con nên luôn trả replyCount bằng 0.
        return toResponse(comment, 0L);
    }

}
