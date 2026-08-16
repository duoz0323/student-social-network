package com.stu.edu.vn.backend.admin.rbac.repository;

import com.stu.edu.vn.backend.admin.rbac.entity.AdminRole;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AdminRoleRepository extends JpaRepository<AdminRole, Long> {
    Optional<AdminRole> findByCode(String code);
    boolean existsByCode(String code);
    List<AdminRole> findAllByCodeIn(Collection<String> codes);
    List<AdminRole> findAllByOrderByIdAsc();

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select r from AdminRole r where r.code = :code")
    Optional<AdminRole> findByCodeForUpdate(@Param("code") String code);
}
