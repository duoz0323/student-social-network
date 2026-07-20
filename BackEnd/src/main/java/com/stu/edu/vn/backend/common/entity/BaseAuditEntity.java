package com.stu.edu.vn.backend.common.entity;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import java.time.LocalDateTime;
import lombok.Getter;

/**
 * Ánh xạ hai cột audit chung do MySQL tự quản lý bằng DEFAULT và ON UPDATE.
 */
@MappedSuperclass
@Getter
public abstract class BaseAuditEntity {

    @Column(name = "created_at", nullable = false, insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false, insertable = false, updatable = false)
    private LocalDateTime updatedAt;

}
