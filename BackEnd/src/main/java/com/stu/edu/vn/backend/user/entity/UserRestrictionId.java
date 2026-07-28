package com.stu.edu.vn.backend.user.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

/** Khóa kép định danh duy nhất quan hệ hạn chế một chiều. */
@Embeddable
public class UserRestrictionId implements Serializable {
    @Column(name = "restrictor_id", nullable = false)
    private Long restrictorId;
    @Column(name = "restricted_id", nullable = false)
    private Long restrictedId;

    protected UserRestrictionId() { }

    public UserRestrictionId(Long restrictorId, Long restrictedId) {
        this.restrictorId = restrictorId;
        this.restrictedId = restrictedId;
    }

    public Long getRestrictorId() { return restrictorId; }
    public Long getRestrictedId() { return restrictedId; }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (!(object instanceof UserRestrictionId that)) return false;
        return Objects.equals(restrictorId, that.restrictorId)
                && Objects.equals(restrictedId, that.restrictedId);
    }

    @Override
    public int hashCode() { return Objects.hash(restrictorId, restrictedId); }
}
