package com.stu.edu.vn.backend.academic.entity;

import com.stu.edu.vn.backend.academic.enums.AcademicStatus;
import com.stu.edu.vn.backend.common.entity.BaseAuditEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** Khoa luôn thuộc đúng một trường trong master data học thuật. */
@Entity
@Table(name = "faculties")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Faculty extends BaseAuditEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "school_id", nullable = false)
    private School school;

    @Column(nullable = false, length = 255)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private AcademicStatus status;

    /** Faculty được tạo trong đúng School và không tự thay đổi quan hệ cha khi cập nhật V1. */
    public Faculty(School school, String name) {
        this.school = school;
        this.name = name;
        this.status = AcademicStatus.ACTIVE;
    }

    public void updateName(String name) {
        this.name = name;
    }

    public void changeStatus(AcademicStatus status) {
        this.status = status;
    }
}
