package com.stu.edu.vn.backend.academic.entity;

import com.stu.edu.vn.backend.academic.enums.AcademicStatus;
import com.stu.edu.vn.backend.common.entity.BaseAuditEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** Danh mục sở thích chuẩn hóa, không lưu text tự do trong hồ sơ người dùng. */
@Entity
@Table(name = "interest_categories")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class InterestCategory extends BaseAuditEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private AcademicStatus status;
}
