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

/** Quan hệ Restrict một chiều, không làm thay đổi quyền xem hoặc tương tác. */
@Entity
@Table(name = "user_restrictions")
public class UserRestriction {
    @EmbeddedId
    private UserRestrictionId id;
    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("restrictorId")
    @JoinColumn(name = "restrictor_id", nullable = false)
    private User restrictor;
    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("restrictedId")
    @JoinColumn(name = "restricted_id", nullable = false)
    private User restricted;
    @Column(name = "created_at", nullable = false, insertable = false, updatable = false)
    private LocalDateTime createdAt;

    protected UserRestriction() { }

    public UserRestriction(User restrictor, User restricted) {
        this.restrictor = restrictor;
        this.restricted = restricted;
        this.id = new UserRestrictionId(restrictor.getId(), restricted.getId());
    }

    public UserRestrictionId getId() { return id; }
    public User getRestrictor() { return restrictor; }
    public User getRestricted() { return restricted; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
