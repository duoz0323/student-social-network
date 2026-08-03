package com.stu.edu.vn.backend.analytics.tracking;

import com.stu.edu.vn.backend.analytics.entity.UserDailyActivity;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

/**
 * Repository ghi activity bằng UPDATE/UPSERT atomic để không phát sinh race condition.
 */
public interface UserActivityTrackingRepository extends Repository<UserDailyActivity, Long> {

    @Modifying
    @Query(value = """
            UPDATE users
            SET first_active_at = CASE
                    WHEN first_active_at IS NULL OR first_active_at > :activeAt THEN :activeAt
                    ELSE first_active_at
                END,
                last_active_at = CASE
                    WHEN last_active_at IS NULL OR last_active_at < :activeAt THEN :activeAt
                    ELSE last_active_at
                END
            WHERE id = :userId
              AND role = 'USER'
              AND status = 'ACTIVE'
            """, nativeQuery = true)
    int updateUserActivityBounds(@Param("userId") Long userId, @Param("activeAt") LocalDateTime activeAt);

    @Modifying
    @Query(value = """
            INSERT INTO user_daily_activities (
                user_id, activity_date, first_active_at, last_active_at, activity_count
            ) VALUES (
                :userId, :activityDate, :activeAt, :activeAt, 1
            )
            ON DUPLICATE KEY UPDATE
                first_active_at = LEAST(first_active_at, VALUES(first_active_at)),
                last_active_at = GREATEST(last_active_at, VALUES(last_active_at)),
                activity_count = activity_count + 1,
                updated_at = CURRENT_TIMESTAMP(6)
            """, nativeQuery = true)
    void upsertDailyActivity(
            @Param("userId") Long userId,
            @Param("activityDate") LocalDate activityDate,
            @Param("activeAt") LocalDateTime activeAt
    );
}
