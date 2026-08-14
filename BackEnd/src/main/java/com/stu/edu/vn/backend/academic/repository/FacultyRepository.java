package com.stu.edu.vn.backend.academic.repository;

import com.stu.edu.vn.backend.academic.entity.Faculty;
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

/** Truy vấn Faculty theo School ngay tại database để giữ đúng hierarchy. */
public interface FacultyRepository extends JpaRepository<Faculty, Long> {
    Optional<Faculty> findByIdAndStatus(Long id, AcademicStatus status);

    boolean existsBySchoolIdAndName(Long schoolId, String name);

    boolean existsBySchoolIdAndNameAndIdNot(Long schoolId, String name, Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select faculty from Faculty faculty join fetch faculty.school where faculty.id = :id")
    Optional<Faculty> findByIdForUpdate(@Param("id") Long id);

    @Query("""
            select faculty from Faculty faculty
            where faculty.school.id = :schoolId
              and (:keyword = '' or faculty.name like concat('%', :keyword, '%') escape '=')
            order by faculty.name asc, faculty.id asc
            """)
    Page<Faculty> searchForAdminBySchool(
            @Param("schoolId") Long schoolId,
            @Param("keyword") String keyword,
            Pageable pageable
    );

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

    @Query("""
            select faculty from Faculty faculty
            where faculty.id = :id and faculty.status = :status and faculty.school.status = :status
            """)
    Optional<Faculty> findSelectableById(
            @Param("id") Long id,
            @Param("status") AcademicStatus status
    );
}
