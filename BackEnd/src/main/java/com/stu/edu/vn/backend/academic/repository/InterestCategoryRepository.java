package com.stu.edu.vn.backend.academic.repository;

import com.stu.edu.vn.backend.academic.entity.InterestCategory;
import com.stu.edu.vn.backend.academic.enums.AcademicStatus;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/** Repository danh mục sở thích; profile chỉ chấp nhận các ID ACTIVE. */
public interface InterestCategoryRepository extends JpaRepository<InterestCategory, Long> {
    List<InterestCategory> findAllByStatusOrderByNameAsc(AcademicStatus status);

    List<InterestCategory> findAllByIdInAndStatus(Collection<Long> ids, AcademicStatus status);
}
