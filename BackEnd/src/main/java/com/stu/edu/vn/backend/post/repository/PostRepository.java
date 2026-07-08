package com.stu.edu.vn.backend.post.repository;

import com.stu.edu.vn.backend.post.entity.Post;
import com.stu.edu.vn.backend.post.enums.PostStatus;
import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Repository truy vấn bài viết theo id, trạng thái và tác giả mà không chứa nghiệp vụ.
 */
public interface PostRepository extends JpaRepository<Post, Long> {

    // Tìm bài theo id và trạng thái để loại bài HIDDEN/DELETED khỏi truy vấn thông thường.
    Optional<Post> findByIdAndStatus(Long id, PostStatus status);

    // Kiểm tra quyền sở hữu bài viết trước khi cho phép tác giả sửa hoặc xóa mềm.
    boolean existsByIdAndAuthor_Id(Long id, Long authorId);

    // Fetch tác giả và hồ sơ tác giả cho API chi tiết, còn media/hashtag tải bằng repository riêng.
    @EntityGraph(attributePaths = {"author", "authorProfile"})
    @Query("select p from Post p where p.id = :postId and p.status = :status")
    Optional<Post> findDetailHeaderByIdAndStatus(
            @Param("postId") Long postId,
            @Param("status") PostStatus status
    );

    // Cập nhật cờ đã chỉnh sửa và updated_at ngay cả khi người dùng chỉ đổi hashtag hoặc ảnh.
    @Modifying
    @Query(
            value = "UPDATE posts SET is_edited = TRUE, updated_at = CURRENT_TIMESTAMP(6) WHERE id = :postId",
            nativeQuery = true
    )
    void markEdited(@Param("postId") Long postId);

    // Xóa mềm chỉ đổi trạng thái bài PUBLISHED, không xóa cứng posts hoặc dữ liệu liên quan.
    @Modifying
    @Query(
            value = """
                    UPDATE posts
                    SET status = 'DELETED',
                        deleted_at = :deletedAt,
                        updated_at = CURRENT_TIMESTAMP(6)
                    WHERE id = :postId
                      AND status = 'PUBLISHED'
                    """,
            nativeQuery = true
    )
    int softDeletePublishedPost(
            @Param("postId") Long postId,
            @Param("deletedAt") LocalDateTime deletedAt
    );
}
