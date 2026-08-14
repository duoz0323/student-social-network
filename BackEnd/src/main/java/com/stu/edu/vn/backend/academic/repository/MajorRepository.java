package com.stu.edu.vn.backend.academic.repository;

import com.stu.edu.vn.backend.academic.entity.Major;
import com.stu.edu.vn.backend.academic.enums.AcademicStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;

/** Truy vấn Major theo Faculty ngay tại database để giữ đúng hierarchy. */
public interface MajorRepository extends JpaRepository<Major, Long> {
    Optional<Major> findByIdAndStatus(Long id, AcademicStatus status);

    boolean existsByFacultyIdAndName(Long facultyId, String name);

    boolean existsByFacultyIdAndNameAndIdNot(Long facultyId, String name, Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select major from Major major join fetch major.faculty where major.id = :id")
    Optional<Major> findByIdForUpdate(@Param("id") Long id);

    @Query("""
            select major from Major major
            where major.faculty.id = :facultyId
              and (:keyword = '' or major.name like concat('%', :keyword, '%') escape '=')
            order by major.name asc, major.id asc
            """)
    Page<Major> searchForAdminByFaculty(
            @Param("facultyId") Long facultyId,
            @Param("keyword") String keyword,
            Pageable pageable
    );

    @Query("""
            select major from Major major
            where major.faculty.id = :facultyId and major.status = :status
              and major.faculty.school.status = :status
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
