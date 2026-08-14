package com.stu.edu.vn.backend.discovery.repository;

import com.stu.edu.vn.backend.discovery.cursor.MapLocationPostsCursor;
import com.stu.edu.vn.backend.discovery.dto.response.MapLocationResponse;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

/** Thực hiện aggregation marker và keyset Location Posts hoàn toàn tại MySQL. */
@Repository
@RequiredArgsConstructor
public class DiscoveryMapRepository {
    static final String FIND_MAP_LOCATIONS_SQL = """
            SELECT l.id AS location_id,
                   l.display_name,
                   l.formatted_address,
                   l.latitude,
                   l.longitude,
                   COUNT(p.id) AS post_count,
                   MAX(p.published_at) AS latest_post_at
            FROM locations l
            JOIN posts p ON p.location_id = l.id
            JOIN users author ON author.id = p.author_id
            JOIN user_profiles author_profile ON author_profile.user_id = p.author_id
            WHERE l.latitude BETWEEN :south AND :north
              AND l.longitude BETWEEN :west AND :east
              AND p.status = 'PUBLISHED'
              AND author.role = 'USER'
              AND author.status = 'ACTIVE'
              AND author_profile.profile_completed_at IS NOT NULL
              AND NOT EXISTS (
                  SELECT 1
                  FROM user_blocks viewer_block
                  WHERE viewer_block.blocker_id = :viewerId
                    AND viewer_block.blocked_id = p.author_id
              )
              AND NOT EXISTS (
                  SELECT 1
                  FROM user_blocks author_block
                  WHERE author_block.blocker_id = p.author_id
                    AND author_block.blocked_id = :viewerId
              )
            GROUP BY l.id, l.display_name, l.formatted_address, l.latitude, l.longitude
            ORDER BY latest_post_at DESC, l.id DESC
            LIMIT :resultLimit
            """;

    static final String FIND_LOCATION_POST_KEYS_SQL = """
            SELECT p.id AS post_id,
                   p.published_at
            FROM posts p
            JOIN users author ON author.id = p.author_id
            JOIN user_profiles author_profile ON author_profile.user_id = p.author_id
            WHERE p.location_id = :locationId
              AND p.status = 'PUBLISHED'
              AND author.role = 'USER'
              AND author.status = 'ACTIVE'
              AND author_profile.profile_completed_at IS NOT NULL
              AND NOT EXISTS (
                  SELECT 1
                  FROM user_blocks viewer_block
                  WHERE viewer_block.blocker_id = :viewerId
                    AND viewer_block.blocked_id = p.author_id
              )
              AND NOT EXISTS (
                  SELECT 1
                  FROM user_blocks author_block
                  WHERE author_block.blocker_id = p.author_id
                    AND author_block.blocked_id = :viewerId
              )
              AND (
                  :hasCursor = 0
                  OR p.published_at < :cursorPublishedAt
                  OR (p.published_at = :cursorPublishedAt AND p.id < :cursorPostId)
              )
            ORDER BY p.published_at DESC, p.id DESC
            LIMIT :resultLimit
            """;

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public List<MapLocationResponse> findMapLocations(
            Long viewerId,
            double north,
            double south,
            double east,
            double west,
            int resultLimit
    ) {
        MapSqlParameterSource parameters = new MapSqlParameterSource()
                .addValue("viewerId", viewerId)
                .addValue("north", north)
                .addValue("south", south)
                .addValue("east", east)
                .addValue("west", west)
                .addValue("resultLimit", resultLimit);
        return jdbcTemplate.query(FIND_MAP_LOCATIONS_SQL, parameters, (resultSet, rowNumber) ->
                new MapLocationResponse(
                        resultSet.getLong("location_id"),
                        resultSet.getString("display_name"),
                        resultSet.getString("formatted_address"),
                        resultSet.getBigDecimal("latitude"),
                        resultSet.getBigDecimal("longitude"),
                        resultSet.getLong("post_count"),
                        resultSet.getTimestamp("latest_post_at").toLocalDateTime()
                ));
    }

    public List<MapLocationPostKey> findLocationPostKeys(
            Long viewerId,
            Long locationId,
            MapLocationPostsCursor cursor,
            int resultLimit
    ) {
        boolean hasCursor = cursor != null;
        MapSqlParameterSource parameters = new MapSqlParameterSource()
                .addValue("viewerId", viewerId)
                .addValue("locationId", locationId)
                .addValue("hasCursor", hasCursor ? 1 : 0)
                .addValue("cursorPublishedAt", Timestamp.valueOf(hasCursor
                        ? cursor.publishedAt()
                        : LocalDateTime.of(9999, 12, 31, 23, 59, 59)))
                .addValue("cursorPostId", hasCursor ? cursor.postId() : Long.MAX_VALUE)
                .addValue("resultLimit", resultLimit);
        return jdbcTemplate.query(FIND_LOCATION_POST_KEYS_SQL, parameters, (resultSet, rowNumber) ->
                new MapLocationPostKey(
                        resultSet.getLong("post_id"),
                        resultSet.getTimestamp("published_at").toLocalDateTime()
                ));
    }
}
