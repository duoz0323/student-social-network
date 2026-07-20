package com.stu.edu.vn.backend.auth.entity;

import com.stu.edu.vn.backend.auth.enums.AuthProvider;
import com.stu.edu.vn.backend.common.entity.BaseAuditEntity;
import com.stu.edu.vn.backend.user.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Liên kết một danh tính Google/Facebook đã được Backend xác minh với tài khoản nội bộ.
 */
@Entity
@Table(
        name = "user_auth_providers",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uq_user_auth_provider_identity",
                        columnNames = {"provider", "provider_user_id"}
                ),
                @UniqueConstraint(
                        name = "uq_user_auth_provider_per_user",
                        columnNames = {"user_id", "provider"}
                )
        },
        indexes = @Index(
                name = "idx_user_auth_providers_user",
                columnList = "user_id,provider,id"
        )
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserAuthProvider extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "provider", nullable = false, length = 16)
    private AuthProvider provider;

    @Column(name = "provider_user_id", nullable = false)
    private String providerUserId;

    @Column(name = "provider_email", nullable = true)
    private String providerEmail;

    @Column(name = "provider_email_verified", nullable = true)
    private Boolean providerEmailVerified;

    public UserAuthProvider(
            User user,
            AuthProvider provider,
            String providerUserId,
            String providerEmail,
            Boolean providerEmailVerified
    ) {
        this.user = Objects.requireNonNull(user, "user không được null");
        this.provider = Objects.requireNonNull(provider, "provider không được null");
        this.providerUserId = requireText(providerUserId, "providerUserId");
        validateProviderEmail(providerEmail, providerEmailVerified);
        this.providerEmail = providerEmail;
        this.providerEmailVerified = providerEmailVerified;
    }

    private static void validateProviderEmail(String email, Boolean verified) {
        if ((email == null) != (verified == null)) {
            throw new IllegalArgumentException("providerEmail và providerEmailVerified phải cùng null hoặc cùng có giá trị");
        }
    }

    private static String requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " không được để trống");
        }
        return value;
    }
}
