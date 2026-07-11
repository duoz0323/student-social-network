package com.stu.edu.vn.backend.post.repository;

import com.stu.edu.vn.backend.post.entity.PostLike;
import com.stu.edu.vn.backend.post.entity.PostLikeId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Repository truy cập bảng post_likes, chỉ xử lý lưu/xóa/kiểm tra quan hệ Like.
 */
public interface PostLikeRepository extends JpaRepository<PostLike, PostLikeId> {

    // Kiểm tra Like trùng trước khi insert để trả lỗi nghiệp vụ rõ ràng cho Client.
    boolean existsByIdUserIdAndIdPostId(Long userId, Long postId);

    // Xóa Like bằng khóa chính kép và trả số dòng bị ảnh hưởng để phát hiện trường hợp chưa từng Like.
    @Modifying
    @Query(
            value = "DELETE FROM post_likes WHERE user_id = :userId AND post_id = :postId",
            nativeQuery = true
    )
    int deleteByUserIdAndPostId(
            @Param("userId") Long userId,
            @Param("postId") Long postId
    );
}
