package com.stu.edu.vn.backend.post.repository;

import com.stu.edu.vn.backend.post.entity.PostHashtag;
import com.stu.edu.vn.backend.post.entity.PostHashtagId;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Repository quản lý quan hệ bài viết - hashtag, chỉ xóa quan hệ và không xóa hashtag dùng chung.
 */
public interface PostHashtagRepository extends JpaRepository<PostHashtag, PostHashtagId> {

    // Lấy toàn bộ quan hệ hashtag của một bài để phục vụ cập nhật hoặc kiểm tra.
    List<PostHashtag> findByPost_Id(Long postId);

    // Xóa quan hệ hashtag của bài khi cập nhật hashtag, không xóa bản ghi Hashtag dùng chung.
    @Modifying
    @Query("delete from PostHashtag ph where ph.post.id = :postId")
    void deleteByPostId(@Param("postId") Long postId);

    // Fetch sẵn Hashtag để mapper/service không gây N+1 khi trả danh sách hashtag của bài.
    @EntityGraph(attributePaths = "hashtag")
    @Query("select ph from PostHashtag ph where ph.post.id = :postId")
    List<PostHashtag> findWithHashtagByPostId(@Param("postId") Long postId);
}
