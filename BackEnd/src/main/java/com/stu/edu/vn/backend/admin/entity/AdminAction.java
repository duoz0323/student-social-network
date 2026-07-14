package com.stu.edu.vn.backend.admin.entity;

import com.stu.edu.vn.backend.admin.enums.AdminActionType;
import com.stu.edu.vn.backend.admin.enums.AdminTargetType;
import com.stu.edu.vn.backend.user.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

/**
 * Entity lưu dấu vết thao tác quản trị tổng quát trên nhiều loại đối tượng.
 */
@Entity
@Table(name = "admin_actions")
public class AdminAction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "admin_id", nullable = false)
    private User admin;

    @Enumerated(EnumType.STRING)
    @Column(name = "action_type", nullable = false)
    private AdminActionType actionType;

    @Enumerated(EnumType.STRING)
    @Column(name = "target_type", nullable = false)
    private AdminTargetType targetType;

    @Column(name = "target_id", nullable = false)
    private Long targetId;

    @Column(name = "note", length = 1000)
    private String note;

    @Column(name = "old_data", columnDefinition = "json")
    private String oldData;

    @Column(name = "new_data", columnDefinition = "json")
    private String newData;

    @Column(name = "created_at", nullable = false, insertable = false, updatable = false)
    private LocalDateTime createdAt;

    protected AdminAction() {
        // Constructor rỗng dành cho JPA.
    }

    public AdminAction(
            User admin,
            AdminActionType actionType,
            AdminTargetType targetType,
            Long targetId,
            String note
    ) {
        this.admin = admin;
        this.actionType = actionType;
        this.targetType = targetType;
        this.targetId = targetId;
        this.note = note;
        // oldData và newData chủ động để null trong phạm vi quản lý trạng thái tài khoản hiện tại.
        this.oldData = null;
        this.newData = null;
    }

    public Long getId() {
        return id;
    }

    public User getAdmin() {
        return admin;
    }

    public AdminActionType getActionType() {
        return actionType;
    }

    public AdminTargetType getTargetType() {
        return targetType;
    }

    public Long getTargetId() {
        return targetId;
    }

    public String getNote() {
        return note;
    }

    public String getOldData() {
        return oldData;
    }

    public String getNewData() {
        return newData;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
