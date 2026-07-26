package com.stu.edu.vn.backend.user.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

/** Khóa kép định danh duy nhất một quan hệ chặn có hướng giữa hai người dùng. */
@Embeddable
public class UserBlockId implements Serializable {
    @Column(name = "blocker_id", nullable = false)
    private Long blockerId;
    @Column(name = "blocked_id", nullable = false)
    private Long blockedId;

    protected UserBlockId() { /* Constructor rỗng dành cho JPA. */ }

    public UserBlockId(Long blockerId, Long blockedId) {
        this.blockerId = blockerId;
        this.blockedId = blockedId;
    }

    public Long getBlockerId() { return blockerId; }
    public Long getBlockedId() { return blockedId; }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (!(object instanceof UserBlockId that)) return false;
        return Objects.equals(blockerId, that.blockerId) && Objects.equals(blockedId, that.blockedId);
    }

    @Override
    public int hashCode() { return Objects.hash(blockerId, blockedId); }
}
