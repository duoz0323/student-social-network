package com.stu.edu.vn.backend.interaction.repository;

import com.stu.edu.vn.backend.interaction.entity.Comment;
import com.stu.edu.vn.backend.interaction.enums.CommentStatus;
import com.stu.edu.vn.backend.interaction.repository.projection.CommentReplyCountProjection;
import jakarta.persistence.LockModeType;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Repository truy cập bảng comments, chỉ chịu trách nhiệm truy vấn và cập nhật dữ liệu bình luận.
 */
public interface CommentRepository extends JpaRepository<Comment, Long> {

    // Lấy bình luận gốc đang hiển thị hoặc tombstone còn reply, có phân trang để tránh tải toàn bộ hội thoại.
    @EntityGraph(attributePaths = {"author", "authorProfile", "post"})
    @Query(
            value = """
                    SELECT comment
                    FROM Comment comment
                    WHERE comment.post.id = :postId
                      AND comment.parentComment IS NULL
                      AND (
                          comment.status = :publishedStatus
                          OR EXISTS (
                              SELECT reply.id
                              FROM Comment reply
                              WHERE reply.parentComment = comment
                                AND reply.status = :publishedStatus
                          )
                      )
                    ORDER BY comment.createdAt ASC, comment.id ASC
                    """,
            countQuery = """
                    SELECT COUNT(comment.id)
                    FROM Comment comment
                    WHERE comment.post.id = :postId
                      AND comment.parentComment IS NULL
                      AND (
                          comment.status = :publishedStatus
                          OR EXISTS (
                              SELECT reply.id
                              FROM Comment reply
                              WHERE reply.parentComment = comment
                                AND reply.status = :publishedStatus
                          )
                      )
                    """
    )
    Page<Comment> findVisibleRootComments(
            @Param("postId") Long postId,
            @Param("publishedStatus") CommentStatus publishedStatus,
            Pageable pageable
    );

    // Lấy reply PUBLISHED của một bình luận gốc theo thứ tự thời gian tăng dần.
    @EntityGraph(attributePaths = {"author", "authorProfile", "post", "parentComment"})
    Page<Comment> findByParentComment_IdAndStatusOrderByCreatedAtAscIdAsc(
            Long parentCommentId,
            CommentStatus status,
            Pageable pageable
    );

    // Lấy bình luận kèm bài viết để kiểm tra trạng thái khi tạo hoặc đọc reply.
    @EntityGraph(attributePaths = {"post", "parentComment"})
    @Query("SELECT comment FROM Comment comment WHERE comment.id = :commentId")
    Optional<Comment> findWithPostAndParentById(@Param("commentId") Long commentId);

    // Khóa bình luận cha trong transaction tạo reply để tránh tạo reply đồng thời với thao tác xóa cha.
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @EntityGraph(attributePaths = {"post", "parentComment"})
    @Query("SELECT comment FROM Comment comment WHERE comment.id = :commentId")
    Optional<Comment> findForReplyCreationById(@Param("commentId") Long commentId);

    // Gom số reply theo các bình luận gốc trong một truy vấn thay vì đếm riêng từng bình luận.
    @Query("""
            SELECT reply.parentComment.id AS commentId, COUNT(reply.id) AS replyCount
            FROM Comment reply
            WHERE reply.parentComment.id IN :commentIds
              AND reply.status = :status
            GROUP BY reply.parentComment.id
            """)
    List<CommentReplyCountProjection> countRepliesByParentIdsAndStatus(
            @Param("commentIds") List<Long> commentIds,
            @Param("status") CommentStatus status
    );

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
