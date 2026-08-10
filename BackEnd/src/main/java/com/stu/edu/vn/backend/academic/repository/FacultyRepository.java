package com.stu.edu.vn.backend.academic.repository;

import com.stu.edu.vn.backend.academic.entity.Faculty;
import com.stu.edu.vn.backend.academic.enums.AcademicStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/** Truy vấn Faculty theo School ngay tại database để giữ đúng hierarchy. */
public interface FacultyRepository extends JpaRepository<Faculty, Long> {
    Optional<Faculty> findByIdAndStatus(Long id, AcademicStatus status);

    @Query("""
            select faculty from Faculty faculty
            where faculty.school.id = :schoolId and faculty.status = :status
              and (:keyword = '' or faculty.name like concat(:keyword, '%') escape '=')
            order by faculty.name asc, faculty.id asc
            """)
    List<Faculty> searchActiveBySchool(
            @Param("schoolId") Long schoolId,
            @Param("status") AcademicStatus status,
            @Param("keyword") String keyword,
            Pageable pageable
    );
}
