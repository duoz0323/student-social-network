package com.stu.edu.vn.backend.discovery.repository;

import com.stu.edu.vn.backend.discovery.cursor.NearbyCursor;
import com.stu.edu.vn.backend.discovery.model.NearbyBoundingBox;
import java.sql.Timestamp;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

/** Native MySQL query thực hiện candidate filter, distance, Block, keyset và limit hoàn toàn tại database. */
@Repository
@RequiredArgsConstructor
public class NearbyDiscoveryRepository {
    static final String FIND_NEARBY_SQL = """
            WITH distance_candidates AS (
                SELECT p.id AS post_id,
                       p.published_at,
                       2 * 6371008.8 * ASIN(SQRT(LEAST(1.0,
                           POWER(SIN(RADIANS(l.latitude - :latitude) / 2), 2)
                           + COS(RADIANS(:latitude)) * COS(RADIANS(l.latitude))
                           * POWER(SIN(RADIANS(l.longitude - :longitude) / 2), 2)
                       ))) AS exact_distance_meters
                FROM posts p
                JOIN locations l ON l.id = p.location_id
                JOIN users author ON author.id = p.author_id
                JOIN user_profiles author_profile ON author_profile.user_id = p.author_id
                WHERE p.status = 'PUBLISHED'
                  AND p.location_id IS NOT NULL
                  AND author.role = 'USER'
                  AND author.status = 'ACTIVE'
                  AND author_profile.profile_completed_at IS NOT NULL
                  AND l.latitude BETWEEN :minimumLatitude AND :maximumLatitude
                  AND (
                      :allLongitudes = 1
                      OR (:wrapsAntimeridian = 0
                          AND l.longitude BETWEEN :minimumLongitude AND :maximumLongitude)
                      OR (:wrapsAntimeridian = 1
                          AND (l.longitude >= :minimumLongitude OR l.longitude <= :maximumLongitude))
                  )
                  AND NOT EXISTS (
                      SELECT 1
                      FROM user_blocks blocked_relation
                      WHERE (blocked_relation.blocker_id = :viewerId
                             AND blocked_relation.blocked_id = p.author_id)
                         OR (blocked_relation.blocker_id = p.author_id
                             AND blocked_relation.blocked_id = :viewerId)
                  )
            ),
            normalized_candidates AS (
                SELECT post_id,
                       published_at,
                       CAST(ROUND(exact_distance_meters, 0) AS SIGNED) AS distance_meters
                FROM distance_candidates
                WHERE exact_distance_meters <= :radiusMeters
            )
            SELECT post_id, published_at, distance_meters
            FROM normalized_candidates
            WHERE :hasCursor = 0
               OR distance_meters > :cursorDistanceMeters
               OR (distance_meters = :cursorDistanceMeters AND published_at < :cursorPublishedAt)
               OR (distance_meters = :cursorDistanceMeters AND published_at = :cursorPublishedAt
                   AND post_id < :cursorPostId)
            ORDER BY distance_meters ASC, published_at DESC, post_id DESC
            LIMIT :resultLimit
            """;

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public List<NearbyPostRank> findNearby(
            Long viewerId,
            double latitude,
            double longitude,
            int radiusKm,
            NearbyBoundingBox boundingBox,
            NearbyCursor cursor,
            int resultLimit
    ) {
        boolean hasCursor = cursor != null;
        MapSqlParameterSource parameters = new MapSqlParameterSource()
                .addValue("viewerId", viewerId)
                .addValue("latitude", latitude)
                .addValue("longitude", longitude)
                .addValue("radiusMeters", radiusKm * 1000L)
                .addValue("minimumLatitude", boundingBox.minimumLatitude())
                .addValue("maximumLatitude", boundingBox.maximumLatitude())
                .addValue("minimumLongitude", boundingBox.minimumLongitude())
                .addValue("maximumLongitude", boundingBox.maximumLongitude())
                .addValue("allLongitudes", boundingBox.allLongitudes() ? 1 : 0)
                .addValue("wrapsAntimeridian", boundingBox.wrapsAntimeridian() ? 1 : 0)
                .addValue("hasCursor", hasCursor ? 1 : 0)
                .addValue("cursorDistanceMeters", hasCursor ? cursor.distanceMeters() : 0L)
                .addValue("cursorPublishedAt", Timestamp.valueOf(hasCursor
                        ? cursor.publishedAt()
                        : java.time.LocalDateTime.of(9999, 12, 31, 23, 59, 59)))
                .addValue("cursorPostId", hasCursor ? cursor.postId() : Long.MAX_VALUE)
                .addValue("resultLimit", resultLimit);

        return jdbcTemplate.query(FIND_NEARBY_SQL, parameters, (resultSet, rowNumber) ->
                new NearbyPostRank(
                        resultSet.getLong("post_id"),
                        resultSet.getLong("distance_meters"),
                        resultSet.getTimestamp("published_at").toLocalDateTime()
                ));
    }
}
