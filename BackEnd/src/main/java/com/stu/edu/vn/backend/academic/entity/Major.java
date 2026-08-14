package com.stu.edu.vn.backend.academic.entity;

import com.stu.edu.vn.backend.academic.enums.AcademicStatus;
import com.stu.edu.vn.backend.common.entity.BaseAuditEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** Ngành học luôn thuộc đúng một khoa trong master data học thuật. */
@Entity
@Table(name = "majors")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Major extends BaseAuditEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "faculty_id", nullable = false)
    private Faculty faculty;

    @Column(nullable = false, length = 255)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private AcademicStatus status;

    /** Major được tạo trong đúng Faculty và giữ nguyên quan hệ cha trong phạm vi V1. */
    public Major(Faculty faculty, String name) {
        this.faculty = faculty;
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
