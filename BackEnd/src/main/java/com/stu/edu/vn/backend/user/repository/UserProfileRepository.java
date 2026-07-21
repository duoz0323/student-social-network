package com.stu.edu.vn.backend.user.repository;

import com.stu.edu.vn.backend.user.entity.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository quản lý hồ sơ 1-1 của người dùng.
 */
public interface UserProfileRepository extends JpaRepository<UserProfile, Long> {
}
