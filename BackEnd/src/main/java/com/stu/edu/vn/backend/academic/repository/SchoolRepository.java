package com.stu.edu.vn.backend.academic.repository;

import com.stu.edu.vn.backend.academic.entity.School;
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

/** Truy vấn School tại database, luôn giới hạn kết quả autocomplete. */
public interface SchoolRepository extends JpaRepository<School, Long> {
    Optional<School> findByIdAndStatus(Long id, AcademicStatus status);

    boolean existsByName(String name);

    boolean existsByNameAndIdNot(String name, Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select school from School school where school.id = :id")
    Optional<School> findByIdForUpdate(@Param("id") Long id);

    @Query("""
            select school from School school
            where (:keyword = ''
                   or school.name like concat('%', :keyword, '%') escape '='
                   or coalesce(school.shortName, '') like concat('%', :keyword, '%') escape '=')
            order by school.name asc, school.id asc
            """)
    Page<School> searchForAdmin(@Param("keyword") String keyword, Pageable pageable);

    @Query("""
            select school from School school
            where school.status = :status
              and (:keyword = ''
                   or school.name like concat(:keyword, '%') escape '='
                   or coalesce(school.shortName, '') like concat(:keyword, '%') escape '=')
            order by school.name asc, school.id asc
            """)
    List<School> searchActive(
            @Param("status") AcademicStatus status,
            @Param("keyword") String keyword,
            Pageable pageable
    );
}
