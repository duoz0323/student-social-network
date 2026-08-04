package com.stu.edu.vn.backend.user.repository;

import com.stu.edu.vn.backend.user.entity.User;
import java.util.Optional;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Repository truy vấn tài khoản theo các định danh đăng nhập đã chuẩn hóa.
 */
public interface UserRepository extends JpaRepository<User, Long> {

    boolean existsByEmail(String email);

    Optional<User> findByEmail(String email);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select user from User user where user.id = :id")
    Optional<User> findByIdForUpdate(@Param("id") Long id);

    /** Khóa hai user theo thứ tự ID ổn định để Block và Messaging không chạy xuyên qua nhau. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select user from User user where user.id in :ids order by user.id asc")
    List<User> findPairForUpdate(@Param("ids") List<Long> ids);
}
