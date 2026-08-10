package com.stu.edu.vn.backend.user.repository;

import com.stu.edu.vn.backend.user.entity.UserProfile;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Repository quản lý hồ sơ 1-1 của người dùng.
 */
public interface UserProfileRepository extends JpaRepository<UserProfile, Long> {

    /** Kiểm tra đầy đủ invariant onboarding mà không tải toàn bộ hồ sơ cho mỗi request. */
    @Query("""
            select (count(profile) > 0)
            from UserProfile profile
            where profile.userId = :userId
              and profile.username is not null
              and profile.profileCompletedAt is not null
            """)
    boolean existsByUserIdAndProfileCompletedAtIsNotNull(@Param("userId") Long userId);

    /** Username luôn được lưu ở dạng lowercase nên derived query có thể so sánh chính xác. */
    boolean existsByUsername(String username);

    Optional<UserProfile> findByUsername(String username);

    /**
     * Khóa hồ sơ trong transaction để các cập nhật onboarding, profile và avatar không ghi đè lẫn nhau.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select profile from UserProfile profile where profile.userId = :userId")
    Optional<UserProfile> findByIdForUpdate(@Param("userId") Long userId);
}
