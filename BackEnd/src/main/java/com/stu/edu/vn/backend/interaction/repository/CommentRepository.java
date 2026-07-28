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

    // Lọc Block ngay trong data/count query để phân trang không tính bình luận mà viewer không được thấy.
    @EntityGraph(attributePaths = {"author", "authorProfile", "post"})
    @Query(
            value = """
                    SELECT comment
                    FROM Comment comment
                    WHERE comment.post.id = :postId
                      AND comment.parentComment IS NULL
                      AND NOT EXISTS (
                          SELECT blockRelation.id.blockerId
                          FROM UserBlock blockRelation
                          WHERE (blockRelation.id.blockerId = :viewerId
                                 AND blockRelation.id.blockedId = comment.author.id)
                             OR (blockRelation.id.blockerId = comment.author.id
                                 AND blockRelation.id.blockedId = :viewerId)
                      )
                      AND (
                          comment.status = :publishedStatus
                          OR EXISTS (
                              SELECT reply.id
                              FROM Comment reply
                              WHERE reply.parentComment = comment
                                AND reply.status = :publishedStatus
                                AND NOT EXISTS (
                                    SELECT replyBlock.id.blockerId
                                    FROM UserBlock replyBlock
                                    WHERE (replyBlock.id.blockerId = :viewerId
                                           AND replyBlock.id.blockedId = reply.author.id)
                                       OR (replyBlock.id.blockerId = reply.author.id
                                           AND replyBlock.id.blockedId = :viewerId)
                                )
                          )
                      )
                    ORDER BY comment.createdAt ASC, comment.id ASC
                    """,
            countQuery = """
                    SELECT COUNT(comment.id)
                    FROM Comment comment
                    WHERE comment.post.id = :postId
                      AND comment.parentComment IS NULL
                      AND NOT EXISTS (
                          SELECT blockRelation.id.blockerId
                          FROM UserBlock blockRelation
                          WHERE (blockRelation.id.blockerId = :viewerId
                                 AND blockRelation.id.blockedId = comment.author.id)
                             OR (blockRelation.id.blockerId = comment.author.id
                                 AND blockRelation.id.blockedId = :viewerId)
                      )
                      AND (
                          comment.status = :publishedStatus
                          OR EXISTS (
                              SELECT reply.id
                              FROM Comment reply
                              WHERE reply.parentComment = comment
                                AND reply.status = :publishedStatus
                                AND NOT EXISTS (
                                    SELECT replyBlock.id.blockerId
                                    FROM UserBlock replyBlock
                                    WHERE (replyBlock.id.blockerId = :viewerId
                                           AND replyBlock.id.blockedId = reply.author.id)
                                       OR (replyBlock.id.blockerId = reply.author.id
                                           AND replyBlock.id.blockedId = :viewerId)
                                )
                          )
                      )
                    """
    )
    Page<Comment> findVisibleRootComments(
            @Param("postId") Long postId,
            @Param("viewerId") Long viewerId,
            @Param("publishedStatus") CommentStatus publishedStatus,
            Pageable pageable
    );

    // Chỉ trả reply PUBLISHED không có Block hai chiều với viewer; countQuery dùng cùng điều kiện.
    @EntityGraph(attributePaths = {"author", "authorProfile", "post", "parentComment"})
    @Query(
            value = """
                    SELECT reply
                    FROM Comment reply
                    WHERE reply.parentComment.id = :parentCommentId
                      AND reply.status = :publishedStatus
                      AND NOT EXISTS (
                          SELECT blockRelation.id.blockerId
                          FROM UserBlock blockRelation
                          WHERE (blockRelation.id.blockerId = :viewerId
                                 AND blockRelation.id.blockedId = reply.author.id)
                             OR (blockRelation.id.blockerId = reply.author.id
                                 AND blockRelation.id.blockedId = :viewerId)
                      )
                    ORDER BY reply.createdAt ASC, reply.id ASC
                    """,
            countQuery = """
                    SELECT COUNT(reply.id)
                    FROM Comment reply
                    WHERE reply.parentComment.id = :parentCommentId
                      AND reply.status = :publishedStatus
                      AND NOT EXISTS (
                          SELECT blockRelation.id.blockerId
                          FROM UserBlock blockRelation
                          WHERE (blockRelation.id.blockerId = :viewerId
                                 AND blockRelation.id.blockedId = reply.author.id)
                             OR (blockRelation.id.blockerId = reply.author.id
                                 AND blockRelation.id.blockedId = :viewerId)
                      )
                    """
    )
    Page<Comment> findVisibleReplies(
            @Param("parentCommentId") Long parentCommentId,
            @Param("viewerId") Long viewerId,
            @Param("publishedStatus") CommentStatus publishedStatus,
            Pageable pageable
    );

    // Lấy bình luận kèm bài viết để kiểm tra trạng thái khi tạo hoặc đọc reply.
    @EntityGraph(attributePaths = {"post", "parentComment", "author"})
    @Query("SELECT comment FROM Comment comment WHERE comment.id = :commentId")
    Optional<Comment> findWithPostAndParentById(@Param("commentId") Long commentId);

    // Khóa bình luận cha trong transaction tạo reply để tránh tạo reply đồng thời với thao tác xóa cha.
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @EntityGraph(attributePaths = {"post", "parentComment", "author"})
    @Query("SELECT comment FROM Comment comment WHERE comment.id = :commentId")
    Optional<Comment> findForReplyCreationById(@Param("commentId") Long commentId);

    // Đếm đúng số reply viewer nhìn thấy để replyCount không tiết lộ tài khoản đang có Block.
    @Query("""
            SELECT reply.parentComment.id AS commentId, COUNT(reply.id) AS replyCount
            FROM Comment reply
            WHERE reply.parentComment.id IN :commentIds
              AND reply.status = :status
              AND NOT EXISTS (
                  SELECT blockRelation.id.blockerId
                  FROM UserBlock blockRelation
                  WHERE (blockRelation.id.blockerId = :viewerId
                         AND blockRelation.id.blockedId = reply.author.id)
                     OR (blockRelation.id.blockerId = reply.author.id
                         AND blockRelation.id.blockedId = :viewerId)
              )
            GROUP BY reply.parentComment.id
            """)
    List<CommentReplyCountProjection> countVisibleRepliesByParentIdsAndStatus(
            @Param("commentIds") List<Long> commentIds,
            @Param("viewerId") Long viewerId,
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
