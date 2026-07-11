package com.stu.edu.vn.backend.interaction.repository;

import com.stu.edu.vn.backend.interaction.entity.Comment;
import com.stu.edu.vn.backend.interaction.enums.CommentStatus;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Repository truy cập bảng comments, chỉ chịu trách nhiệm truy vấn và cập nhật dữ liệu bình luận.
 */
public interface CommentRepository extends JpaRepository<Comment, Long> {

    // Lấy comment đang hiển thị của một bài viết theo thứ tự thời gian tăng dần để người dùng đọc mạch hội thoại.
    @EntityGraph(attributePaths = {"author", "authorProfile", "post"})
    List<Comment> findByPost_IdAndStatusOrderByCreatedAtAscIdAsc(Long postId, CommentStatus status);

    // Xóa mềm comment bằng update để trigger database tự giảm posts.comment_count khi PUBLISHED chuyển sang DELETED.
    @Modifying
    @Query(
            value = """
                    UPDATE comments
                    SET status = 'DELETED',
                        deleted_at = :deletedAt,
                        updated_at = CURRENT_TIMESTAMP(6)
                    WHERE id = :commentId
                      AND status = 'PUBLISHED'
                    """,
            nativeQuery = true
    )
    int softDeletePublishedComment(
            @Param("commentId") Long commentId,
            @Param("deletedAt") LocalDateTime deletedAt
    );
}
