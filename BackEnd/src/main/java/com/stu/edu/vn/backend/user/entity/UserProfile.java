package com.stu.edu.vn.backend.user.entity;

import com.stu.edu.vn.backend.common.entity.BaseAuditEntity;
import com.stu.edu.vn.backend.academic.entity.Faculty;
import com.stu.edu.vn.backend.academic.entity.InterestCategory;
import com.stu.edu.vn.backend.academic.entity.Major;
import com.stu.edu.vn.backend.academic.entity.School;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * Entity user_profiles lưu hồ sơ công khai, tách khỏi dữ liệu xác thực của users.
 */
@Entity
@Table(name = "user_profiles")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserProfile extends BaseAuditEntity {

    @Id
    @Column(name = "user_id")
    @Setter(AccessLevel.NONE)
    private Long userId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "username", length = 30, unique = true)
    private String username;

    @Column(name = "display_name", length = 100)
    private String displayName;

    @Column(name = "avatar_url", length = 1000)
    private String avatarUrl;

    @Column(name = "avatar_public_id")
    private String avatarPublicId;

    @Column(name = "bio", length = 500)
    private String bio;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "school_id")
    private School school;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "faculty_id")
    private Faculty faculty;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "major_id")
    private Major major;

    // API giữ Integer, còn JDBC phải khớp SMALLINT UNSIGNED trong schema MySQL hiện hành.
    @JdbcTypeCode(SqlTypes.SMALLINT)
    @Column(name = "entry_year")
    private Integer entryYear;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "user_interests",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "interest_id")
    )
    private Set<InterestCategory> interests = new HashSet<>();

    @Column(name = "profile_completed_at")
    private LocalDateTime profileCompletedAt;

    public UserProfile(User user) {
        this.user = user;
    }

    /** Hồ sơ chỉ hoàn tất khi timestamp và toàn bộ dữ liệu onboarding bắt buộc cùng tồn tại. */
    @Transient
    public boolean isCompleted() {
        return profileCompletedAt != null
                && username != null;
    }

}
