package com.stu.edu.vn.backend.post.repository;

import com.stu.edu.vn.backend.post.entity.PostMedia;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository tải và quản lý media của bài viết theo đúng post_id, không chứa nghiệp vụ phân quyền.
 */
public interface PostMediaRepository extends JpaRepository<PostMedia, Long> {

    // Lấy ảnh của một bài theo đúng thứ tự hiển thị từ 0 đến 3.
    List<PostMedia> findByPost_IdOrderByDisplayOrderAsc(Long postId);

    // Lấy media theo danh sách id và post_id để kiểm tra media giữ lại có thật sự thuộc bài đang sửa.
    List<PostMedia> findByPost_IdAndIdIn(Long postId, Collection<Long> ids);
}
