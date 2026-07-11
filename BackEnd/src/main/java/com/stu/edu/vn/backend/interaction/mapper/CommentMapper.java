package com.stu.edu.vn.backend.interaction.mapper;

import com.stu.edu.vn.backend.interaction.dto.response.CommentResponse;
import com.stu.edu.vn.backend.interaction.entity.Comment;
import com.stu.edu.vn.backend.user.entity.UserProfile;
import org.springframework.stereotype.Component;

/**
 * Mapper chuyển Comment entity sang DTO để API không trả entity trực tiếp.
 */
@Component
public class CommentMapper {

    public CommentResponse toResponse(Comment comment) {
        UserProfile profile = comment.getAuthorProfile();
        return new CommentResponse(
                comment.getId(),
                comment.getPost().getId(),
                comment.getAuthor().getId(),
                profile == null ? null : profile.getDisplayName(),
                profile == null ? null : profile.getAvatarUrl(),
                comment.getContent(),
                comment.getCreatedAt()
        );
    }
}
