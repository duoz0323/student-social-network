package com.stu.edu.vn.backend.admin.repository;

import com.stu.edu.vn.backend.admin.entity.AdminAction;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository ghi thao tác ADMIN trong cùng transaction với thay đổi nghiệp vụ.
 */
public interface AdminActionRepository extends JpaRepository<AdminAction, Long> {
}
