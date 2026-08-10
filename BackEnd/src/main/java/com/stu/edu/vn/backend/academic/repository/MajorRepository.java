package com.stu.edu.vn.backend.academic.repository;

import com.stu.edu.vn.backend.academic.entity.Major;
import com.stu.edu.vn.backend.academic.enums.AcademicStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/** Truy vấn Major theo Faculty ngay tại database để giữ đúng hierarchy. */
public interface MajorRepository extends JpaRepository<Major, Long> {
    Optional<Major> findByIdAndStatus(Long id, AcademicStatus status);

    @Query("""
            select major from Major major
            where major.faculty.id = :facultyId and major.status = :status
              and (:keyword = '' or major.name like concat(:keyword, '%') escape '=')
            order by major.name asc, major.id asc
            """)
    List<Major> searchActiveByFaculty(
            @Param("facultyId") Long facultyId,
            @Param("status") AcademicStatus status,
            @Param("keyword") String keyword,
            Pageable pageable
    );
}
