package com.stu.edu.vn.backend.storage.cleanup;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

/** Lấy batch ổn định các task đến hạn; từng task được khóa khi xử lý. */
public interface MediaCleanupTaskRepository extends JpaRepository<MediaCleanupTask, Long> {
    @Query("select task.id from MediaCleanupTask task where task.status in :statuses "
            + "and task.nextRetryAt <= :now order by task.nextRetryAt, task.id")
    List<Long> findDueIds(@Param("statuses") Collection<MediaCleanupStatus> statuses, @Param("now") LocalDateTime now, Pageable pageable);

    @Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @Query("select task from MediaCleanupTask task where task.id = :id")
    java.util.Optional<MediaCleanupTask> findByIdForUpdate(@Param("id") Long id);
}
