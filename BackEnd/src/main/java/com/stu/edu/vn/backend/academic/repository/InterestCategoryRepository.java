package com.stu.edu.vn.backend.academic.repository;

import com.stu.edu.vn.backend.academic.entity.InterestCategory;
import com.stu.edu.vn.backend.academic.enums.AcademicStatus;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;

/** Repository danh mục sở thích; profile chỉ chấp nhận các ID ACTIVE. */
public interface InterestCategoryRepository extends JpaRepository<InterestCategory, Long> {
    List<InterestCategory> findAllByStatusOrderByNameAsc(AcademicStatus status);

    List<InterestCategory> findAllByIdInAndStatus(Collection<Long> ids, AcademicStatus status);

    boolean existsByName(String name);

    boolean existsByNameAndIdNot(String name, Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select interest from InterestCategory interest where interest.id = :id")
    Optional<InterestCategory> findByIdForUpdate(@Param("id") Long id);

    @Query("""
            select interest from InterestCategory interest
            where (:keyword = '' or interest.name like concat('%', :keyword, '%') escape '=')
            order by interest.name asc, interest.id asc
            """)
    Page<InterestCategory> searchForAdmin(@Param("keyword") String keyword, Pageable pageable);
}
