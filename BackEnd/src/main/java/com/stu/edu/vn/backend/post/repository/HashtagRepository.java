package com.stu.edu.vn.backend.post.repository;

import com.stu.edu.vn.backend.post.entity.Hashtag;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Repository tìm hashtag theo tên đã chuẩn hóa để tránh tạo trùng hashtag.
 */
public interface HashtagRepository extends JpaRepository<Hashtag, Long> {

    // Tìm một hashtag theo tên đã chuẩn hóa để tái sử dụng bản ghi hiện có.
    Optional<Hashtag> findByNormalizedName(String normalizedName);

    // Truy vấn tối đa 10 gợi ý ngay tại MySQL, ưu tiên prefix rồi độ phổ biến và id ổn định.
    @Query(
            value = """
                    SELECT h.*
                    FROM hashtags h
                    WHERE h.normalized_name LIKE CONCAT('%', :keyword, '%') ESCAPE '='
                    ORDER BY
                      CASE
                        WHEN h.normalized_name LIKE CONCAT(:keyword, '%') ESCAPE '=' THEN 0
                        ELSE 1
                      END,
                      h.post_count DESC,
                      h.id DESC
                    LIMIT 10
                    """,
            nativeQuery = true
    )
    List<Hashtag> findSuggestions(@Param("keyword") String escapedKeyword);

    // Upsert theo unique normalized_name để tránh race condition khi nhiều request tạo cùng hashtag.
    @Modifying
    @Query(
            value = """
                    INSERT INTO hashtags (normalized_name, display_name)
                    VALUES (:normalizedName, :displayName)
                    ON DUPLICATE KEY UPDATE display_name = display_name
                    """,
            nativeQuery = true
    )
    void insertIfAbsent(
            @Param("normalizedName") String normalizedName,
            @Param("displayName") String displayName
    );
}
