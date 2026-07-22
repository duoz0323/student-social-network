package com.stu.edu.vn.backend.admin.repository;

import com.stu.edu.vn.backend.admin.entity.AccountStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository ghi lịch sử thay đổi trạng thái tài khoản trong cùng transaction quản trị.
 */
public interface AccountStatusHistoryRepository extends JpaRepository<AccountStatusHistory, Long> {
}
