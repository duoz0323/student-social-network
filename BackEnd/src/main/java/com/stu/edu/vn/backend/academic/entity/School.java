package com.stu.edu.vn.backend.academic.entity;

import com.stu.edu.vn.backend.academic.enums.AcademicStatus;
import com.stu.edu.vn.backend.common.entity.BaseAuditEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** Master data trường học được chuẩn hóa để profile chỉ lưu khóa ngoại. */
@Entity
@Table(name = "schools")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class School extends BaseAuditEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(name = "short_name", length = 50)
    private String shortName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private AcademicStatus status;
}
