package com.stu.edu.vn.backend.post.repository;

import com.stu.edu.vn.backend.post.entity.SavedPost;
import com.stu.edu.vn.backend.post.entity.SavedPostId;
import java.util.Collection;
import java.util.List;
import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Repository truy cập bảng saved_posts cho thao tác kiểm tra, lưu và bỏ lưu bài viết.
 */
public interface SavedPostRepository extends JpaRepository<SavedPost, SavedPostId> {

    // Kiểm tra trước giúp Save lặp lại trả thành công mà không thực hiện thêm câu INSERT.
    boolean existsByIdUserIdAndIdPostId(Long userId, Long postId);

    // Chỉ lấy khóa post đã lưu cho cả trang, không truy vấn trạng thái Save trong vòng lặp mapping.
    @Query("select sp.id.postId from SavedPost sp where sp.id.userId = :userId and sp.id.postId in :postIds")
    List<Long> findSavedPostIds(@Param("userId") Long userId, @Param("postIds") Collection<Long> postIds);

    @Query("select sp.createdAt from SavedPost sp where sp.id.userId = :userId and sp.id.postId = :postId")
    Optional<LocalDateTime> findCreatedAt(@Param("userId") Long userId, @Param("postId") Long postId);

    @Modifying
    @Query(
            value = "DELETE FROM saved_posts WHERE user_id = :userId AND post_id = :postId",
            nativeQuery = true
    )
    // Xóa trực tiếp đúng một quan hệ; số dòng bằng 0 vẫn là kết quả hợp lệ của Unsave idempotent.
    int deleteByUserIdAndPostId(
            @Param("userId") Long userId,
            @Param("postId") Long postId
    );
}
