package com.stu.edu.vn.backend.interaction.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.stu.edu.vn.backend.interaction.dto.response.CommentResponse;
import com.stu.edu.vn.backend.interaction.entity.Comment;
import com.stu.edu.vn.backend.interaction.enums.CommentStatus;
import com.stu.edu.vn.backend.post.entity.Post;
import com.stu.edu.vn.backend.user.entity.User;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class CommentMapperTest {

    private final CommentMapper commentMapper = new CommentMapper();

    @Test
    void toResponseIncludesParentIdAndReplyCount() {
        Comment parent = comment(90L, null, "Binh luan goc");
        Comment reply = comment(100L, parent, "Tra loi");

        CommentResponse response = commentMapper.toResponse(reply, 0L);

        assertThat(response.parentCommentId()).isEqualTo(90L);
        assertThat(response.replyCount()).isZero();
        assertThat(response.deleted()).isFalse();
        assertThat(response.content()).isEqualTo("Tra loi");
    }

    @Test
    void toResponseHidesAuthorAndContentForDeletedRootTombstone() {
        Comment root = comment(90L, null, "Noi dung da xoa");
        ReflectionTestUtils.setField(root, "status", CommentStatus.DELETED);

        CommentResponse response = commentMapper.toResponse(root, 2L);

        assertThat(response.deleted()).isTrue();
        assertThat(response.replyCount()).isEqualTo(2L);
        assertThat(response.userId()).isNull();
        assertThat(response.displayName()).isNull();
        assertThat(response.avatarUrl()).isNull();
        assertThat(response.content()).isNull();
    }

    private Comment comment(Long commentId, Comment parent, String content) {
        User author = new User("student@example.com", null, "hash");
        ReflectionTestUtils.setField(author, "id", 10L);
        Post post = new Post(author, "Noi dung bai viet");
        ReflectionTestUtils.setField(post, "id", 1L);
        Comment comment = new Comment(post, author, parent, content);
        ReflectionTestUtils.setField(comment, "id", commentId);
        ReflectionTestUtils.setField(comment, "createdAt", LocalDateTime.of(2026, 7, 18, 10, 0));
        return comment;
    }
}
