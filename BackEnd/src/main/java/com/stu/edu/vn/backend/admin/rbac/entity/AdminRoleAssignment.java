package com.stu.edu.vn.backend.admin.rbac.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** Bản ghi gán role, giữ cả người thực hiện để phục vụ truy vết. */
@Entity
@Table(name = "admin_roles")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AdminRoleAssignment {

    @EmbeddedId
    private AdminRoleAssignmentId id;

    @Column(name = "assigned_by")
    private Long assignedBy;

    @Column(name = "assigned_at", nullable = false, insertable = false, updatable = false)
    private LocalDateTime assignedAt;

    public AdminRoleAssignment(Long adminId, Long roleId, Long assignedBy) {
        this.id = new AdminRoleAssignmentId(adminId, roleId);
        this.assignedBy = assignedBy;
    }
}
