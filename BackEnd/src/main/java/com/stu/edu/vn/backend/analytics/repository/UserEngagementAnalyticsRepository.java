package com.stu.edu.vn.backend.analytics.repository;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

/**
 * Repository đẩy toàn bộ phép phân loại và tổng hợp xuống MySQL, không tải lịch sử từng user vào Java.
 */
@Repository
public class UserEngagementAnalyticsRepository {

    private static final String DAILY_INTERACTIONS_QUERY = """
            SELECT activity_date, COALESCE(SUM(activity_count), 0) AS interaction_count
            FROM user_daily_activities
            WHERE activity_date BETWEEN :fromDate AND :toDate
            GROUP BY activity_date
            ORDER BY activity_date ASC
            """;

    private static final String FEATURED_USERS_QUERY = """
            WITH today_activities AS (
                SELECT user_id, SUM(activity_count) AS interaction_count
                FROM user_daily_activities
                WHERE activity_date = :activityDate
                GROUP BY user_id
            ),
            today_posts AS (
                SELECT author_id AS user_id, COUNT(*) AS post_count
                FROM posts
                WHERE status = 'PUBLISHED'
                  AND published_at >= :dayStart
                  AND published_at < :nextDayStart
                GROUP BY author_id
            ),
            candidates AS (
                SELECT user_id FROM today_activities
                UNION
                SELECT user_id FROM today_posts
            )
            SELECT u.id AS user_id,
                   up.display_name,
                   up.avatar_url,
                   COALESCE(tp.post_count, 0) AS post_count,
                   COALESCE(ta.interaction_count, 0) AS interaction_count
            FROM candidates c
            INNER JOIN users u ON u.id = c.user_id
            INNER JOIN user_profiles up ON up.user_id = u.id
            LEFT JOIN today_posts tp ON tp.user_id = u.id
            LEFT JOIN today_activities ta ON ta.user_id = u.id
            WHERE u.role = 'USER'
              AND u.status = 'ACTIVE'
              AND up.profile_completed_at IS NOT NULL
            ORDER BY post_count DESC, interaction_count DESC, u.id ASC
            LIMIT :limit
            """;

    private static final String MONTHLY_QUERY = """
            WITH eligible_users AS (
                SELECT u.id
                FROM users u
                INNER JOIN user_profiles up ON up.user_id = u.id
                WHERE u.role = 'USER'
                  AND u.created_at < :evaluationEndExclusive
                  AND up.profile_completed_at IS NOT NULL
                  AND up.profile_completed_at < :evaluationEndExclusive
                  AND COALESCE(
                      (
                          SELECT ash.old_status
                          FROM account_status_histories ash
                          WHERE ash.user_id = u.id
                            AND ash.created_at >= :evaluationEndExclusive
                          ORDER BY ash.created_at ASC, ash.id ASC
                          LIMIT 1
                      ),
                      u.status
                  ) = 'ACTIVE'
            ),
            activity_bounds AS (
                SELECT eu.id AS user_id,
                       MIN(CASE
                           WHEN uda.activity_date >= :monthStartDate
                            AND uda.activity_date <= :evaluationDate
                           THEN uda.activity_date
                       END) AS first_activity_in_month,
                       MAX(CASE
                           WHEN uda.activity_date < :monthStartDate
                           THEN uda.activity_date
                       END) AS last_activity_before_month
                FROM eligible_users eu
                LEFT JOIN user_daily_activities uda
                       ON uda.user_id = eu.id
                      AND uda.activity_date <= :evaluationDate
                GROUP BY eu.id
            )
            SELECT COUNT(*) AS eligible_system_user_count,
                   COALESCE(SUM(first_activity_in_month IS NOT NULL
                       AND last_activity_before_month IS NULL), 0) AS new_active_user_count,
                   COALESCE(SUM(first_activity_in_month IS NOT NULL
                       AND last_activity_before_month IS NOT NULL
                       AND DATEDIFF(first_activity_in_month, last_activity_before_month) <= :inactiveDays), 0)
                       AS regular_active_user_count,
                   COALESCE(SUM(first_activity_in_month IS NOT NULL
                       AND last_activity_before_month IS NOT NULL
                       AND DATEDIFF(first_activity_in_month, last_activity_before_month) > :inactiveDays), 0)
                       AS returning_user_count,
                   COALESCE(SUM(first_activity_in_month IS NULL
                       AND last_activity_before_month IS NOT NULL
                       AND DATEDIFF(:monthStartDate, last_activity_before_month) <= :inactiveDays), 0)
                       AS recently_inactive_user_count,
                   COALESCE(SUM(first_activity_in_month IS NULL
                       AND last_activity_before_month IS NOT NULL
                       AND DATEDIFF(:monthStartDate, last_activity_before_month) > :inactiveDays), 0)
                       AS eligible_inactive_not_returned_user_count,
                   COALESCE(SUM(first_activity_in_month IS NULL
                       AND last_activity_before_month IS NULL), 0) AS never_active_user_count,
                   COALESCE(SUM(first_activity_in_month IS NOT NULL
                       AND last_activity_before_month IS NOT NULL
                       AND DATEDIFF(:monthStartDate, last_activity_before_month) > :inactiveDays), 0)
                       AS returning_eligible_user_count
            FROM activity_bounds
            """;

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public UserEngagementAnalyticsRepository(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public MonthlyUserEngagementCounts summarizeMonth(
            LocalDateTime monthStart,
            LocalDateTime evaluationEndExclusive,
            int inactiveDays
    ) {
        Map<String, Object> parameters = Map.of(
                "monthStartDate", monthStart.toLocalDate(),
                "evaluationDate", evaluationEndExclusive.toLocalDate().minusDays(1),
                "evaluationEndExclusive", Timestamp.valueOf(evaluationEndExclusive),
                "inactiveDays", inactiveDays
        );
        return jdbcTemplate.queryForObject(MONTHLY_QUERY, parameters, (resultSet, rowNumber) ->
                new MonthlyUserEngagementCounts(
                        resultSet.getLong("eligible_system_user_count"),
                        resultSet.getLong("new_active_user_count"),
                        resultSet.getLong("regular_active_user_count"),
                        resultSet.getLong("returning_user_count"),
                        resultSet.getLong("recently_inactive_user_count"),
                        resultSet.getLong("eligible_inactive_not_returned_user_count"),
                        resultSet.getLong("never_active_user_count"),
                        resultSet.getLong("returning_eligible_user_count")
                ));
    }

    /**
     * Tổng activity_count đã được ghi nhận cho từng ngày; Service sẽ bổ sung các ngày không có dữ liệu.
     */
    public List<DailyInteractionCount> findDailyInteractionCounts(LocalDate fromDate, LocalDate toDate) {
        return jdbcTemplate.query(
                DAILY_INTERACTIONS_QUERY,
                Map.of("fromDate", fromDate, "toDate", toDate),
                (resultSet, rowNumber) -> new DailyInteractionCount(
                        resultSet.getObject("activity_date", LocalDate.class),
                        resultSet.getLong("interaction_count")
                )
        );
    }

    /**
     * Lấy USER nổi bật của ngày UTC hiện tại theo ưu tiên số bài PUBLISHED, sau đó đến lượt tương tác.
     */
    public List<FeaturedUserEngagement> findFeaturedUsers(
            LocalDate activityDate,
            LocalDateTime dayStart,
            LocalDateTime nextDayStart,
            int limit
    ) {
        return jdbcTemplate.query(
                FEATURED_USERS_QUERY,
                Map.of(
                        "activityDate", activityDate,
                        "dayStart", Timestamp.valueOf(dayStart),
                        "nextDayStart", Timestamp.valueOf(nextDayStart),
                        "limit", limit
                ),
                (resultSet, rowNumber) -> new FeaturedUserEngagement(
                        resultSet.getLong("user_id"),
                        resultSet.getString("display_name"),
                        resultSet.getString("avatar_url"),
                        resultSet.getLong("post_count"),
                        resultSet.getLong("interaction_count")
                )
        );
    }
}
