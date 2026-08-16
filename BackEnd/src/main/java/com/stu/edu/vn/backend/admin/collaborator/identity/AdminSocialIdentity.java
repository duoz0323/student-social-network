package com.stu.edu.vn.backend.admin.collaborator.identity;

import com.stu.edu.vn.backend.common.entity.BaseAuditEntity;
import com.stu.edu.vn.backend.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Liên kết nội bộ; API Social tuyệt đối không trả adminId hoặc người tạo liên kết. */
@Entity
@Table(name = "admin_social_identities")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AdminSocialIdentity extends BaseAuditEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter(AccessLevel.NONE)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "admin_id", nullable = false, unique = true)
    private User admin;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "social_user_id", nullable = false, unique = true)
    private User socialUser;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 16)
    @Setter
    private ManagedSocialIdentityStatus status = ManagedSocialIdentityStatus.ACTIVE;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

    public AdminSocialIdentity(User admin, User socialUser, User createdBy) {
        this.admin = admin;
        this.socialUser = socialUser;
        this.createdBy = createdBy;
    }
}
