package com.stu.edu.vn.backend.user.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

/** Quan hệ chặn có hướng; chính sách truy cập áp dụng đối xứng khi tồn tại ở một trong hai chiều. */
@Entity
@Table(name = "user_blocks")
public class UserBlock {
    @EmbeddedId
    private UserBlockId id;
    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("blockerId")
    @JoinColumn(name = "blocker_id", nullable = false)
    private User blocker;
    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("blockedId")
    @JoinColumn(name = "blocked_id", nullable = false)
    private User blocked;
    @Column(name = "created_at", nullable = false, insertable = false, updatable = false)
    private LocalDateTime createdAt;

    protected UserBlock() { /* Constructor rỗng dành cho JPA. */ }

    public UserBlock(User blocker, User blocked) {
        this.blocker = blocker;
        this.blocked = blocked;
        this.id = new UserBlockId(blocker.getId(), blocked.getId());
    }

    public UserBlockId getId() { return id; }
    public User getBlocker() { return blocker; }
    public User getBlocked() { return blocked; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
