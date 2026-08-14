package com.stu.edu.vn.backend.recommendation.repository;

import com.stu.edu.vn.backend.user.entity.UserProfile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/** Truy vấn Recommendation V1 đẩy toàn bộ lọc, tính điểm, sắp xếp và phân trang xuống MySQL. */
public interface StudentRecommendationRepository extends JpaRepository<UserProfile, Long> {

    @Query(value = """
            WITH recommendation_candidates AS (
                SELECT candidate_profile.user_id AS userId,
                       candidate_profile.username AS username,
                       candidate_profile.display_name AS displayName,
                       candidate_profile.avatar_url AS avatarUrl,
                       candidate_profile.school_id AS schoolId,
                       candidate_school.name AS schoolName,
                       candidate_school.short_name AS schoolShortName,
                       candidate_profile.faculty_id AS facultyId,
                       candidate_faculty.name AS facultyName,
                       candidate_profile.major_id AS majorId,
                       candidate_major.name AS majorName,
                       candidate_profile.entry_year AS entryYear,
                       CASE WHEN candidate_profile.school_id = current_profile.school_id
                                  AND candidate_school.status = 'ACTIVE'
                            THEN 1 ELSE 0 END AS sameSchool,
                       CASE WHEN candidate_profile.faculty_id = current_profile.faculty_id
                                  AND candidate_faculty.status = 'ACTIVE'
                                  AND candidate_school.status = 'ACTIVE'
                            THEN 1 ELSE 0 END AS sameFaculty,
                       CASE WHEN candidate_profile.major_id = current_profile.major_id
                                  AND candidate_major.status = 'ACTIVE'
                                  AND candidate_faculty.status = 'ACTIVE'
                                  AND candidate_school.status = 'ACTIVE'
                            THEN 1 ELSE 0 END AS sameMajor,
                       CASE WHEN candidate_profile.entry_year IS NOT NULL
                                  AND candidate_profile.entry_year = current_profile.entry_year
                            THEN 1 ELSE 0 END AS sameEntryYear,
                       (SELECT COUNT(DISTINCT candidate_interest.interest_id)
                        FROM user_interests candidate_interest
                        JOIN user_interests current_interest
                          ON current_interest.user_id = :currentUserId
                         AND current_interest.interest_id = candidate_interest.interest_id
                        JOIN interest_categories interest
                          ON interest.id = candidate_interest.interest_id
                         AND interest.status = 'ACTIVE'
                        WHERE candidate_interest.user_id = candidate_profile.user_id) AS commonInterestCount,
                       (SELECT COUNT(*)
                        FROM follows current_follow
                        JOIN follows candidate_follow
                          ON candidate_follow.follower_id = candidate_profile.user_id
                         AND candidate_follow.following_id = current_follow.following_id
                        JOIN users mutual_user
                          ON mutual_user.id = current_follow.following_id
                         AND mutual_user.role = 'USER'
                         AND mutual_user.status = 'ACTIVE'
                        JOIN user_profiles mutual_profile
                          ON mutual_profile.user_id = mutual_user.id
                         AND mutual_profile.profile_completed_at IS NOT NULL
                        WHERE current_follow.follower_id = :currentUserId) AS mutualConnectionCount
                FROM user_profiles current_profile
                JOIN user_profiles candidate_profile
                  ON candidate_profile.user_id <> current_profile.user_id
                JOIN users candidate_user
                  ON candidate_user.id = candidate_profile.user_id
                 AND candidate_user.role = 'USER'
                 AND candidate_user.status = 'ACTIVE'
                LEFT JOIN schools candidate_school ON candidate_school.id = candidate_profile.school_id
                LEFT JOIN faculties candidate_faculty ON candidate_faculty.id = candidate_profile.faculty_id
                LEFT JOIN majors candidate_major ON candidate_major.id = candidate_profile.major_id
                WHERE current_profile.user_id = :currentUserId
                  AND candidate_profile.profile_completed_at IS NOT NULL
                  AND candidate_profile.username IS NOT NULL
                  AND candidate_profile.display_name IS NOT NULL
                  AND NOT EXISTS (
                      SELECT 1 FROM user_blocks blocked_pair
                      WHERE (blocked_pair.blocker_id = :currentUserId
                             AND blocked_pair.blocked_id = candidate_profile.user_id)
                         OR (blocked_pair.blocker_id = candidate_profile.user_id
                             AND blocked_pair.blocked_id = :currentUserId)
                  )
                  AND NOT EXISTS (
                      SELECT 1 FROM follows existing_follow
                      WHERE existing_follow.follower_id = :currentUserId
                        AND existing_follow.following_id = candidate_profile.user_id
                  )
            ), scored_candidates AS (
                SELECT recommendation_candidates.*,
                       sameSchool * 40
                       + sameFaculty * 25
                       + sameMajor * 20
                       + sameEntryYear * 10
                       + LEAST(commonInterestCount * 5, 25)
                       + LEAST(mutualConnectionCount * 3, 15) AS matchScore
                FROM recommendation_candidates
            )
            SELECT *
            FROM scored_candidates
            WHERE matchScore > 0
            ORDER BY matchScore DESC,
                     commonInterestCount DESC,
                     mutualConnectionCount DESC,
                     userId ASC
            """,
            countQuery = """
            WITH recommendation_candidates AS (
                SELECT candidate_profile.user_id AS userId,
                       CASE WHEN candidate_profile.school_id = current_profile.school_id
                                  AND candidate_school.status = 'ACTIVE'
                            THEN 1 ELSE 0 END AS sameSchool,
                       CASE WHEN candidate_profile.faculty_id = current_profile.faculty_id
                                  AND candidate_faculty.status = 'ACTIVE'
                                  AND candidate_school.status = 'ACTIVE'
                            THEN 1 ELSE 0 END AS sameFaculty,
                       CASE WHEN candidate_profile.major_id = current_profile.major_id
                                  AND candidate_major.status = 'ACTIVE'
                                  AND candidate_faculty.status = 'ACTIVE'
                                  AND candidate_school.status = 'ACTIVE'
                            THEN 1 ELSE 0 END AS sameMajor,
                       CASE WHEN candidate_profile.entry_year IS NOT NULL
                                  AND candidate_profile.entry_year = current_profile.entry_year
                            THEN 1 ELSE 0 END AS sameEntryYear,
                       (SELECT COUNT(DISTINCT candidate_interest.interest_id)
                        FROM user_interests candidate_interest
                        JOIN user_interests current_interest
                          ON current_interest.user_id = :currentUserId
                         AND current_interest.interest_id = candidate_interest.interest_id
                        JOIN interest_categories interest
                          ON interest.id = candidate_interest.interest_id
                         AND interest.status = 'ACTIVE'
                        WHERE candidate_interest.user_id = candidate_profile.user_id) AS commonInterestCount,
                       (SELECT COUNT(*)
                        FROM follows current_follow
                        JOIN follows candidate_follow
                          ON candidate_follow.follower_id = candidate_profile.user_id
                         AND candidate_follow.following_id = current_follow.following_id
                        JOIN users mutual_user
                          ON mutual_user.id = current_follow.following_id
                         AND mutual_user.role = 'USER'
                         AND mutual_user.status = 'ACTIVE'
                        JOIN user_profiles mutual_profile
                          ON mutual_profile.user_id = mutual_user.id
                         AND mutual_profile.profile_completed_at IS NOT NULL
                        WHERE current_follow.follower_id = :currentUserId) AS mutualConnectionCount
                FROM user_profiles current_profile
                JOIN user_profiles candidate_profile
                  ON candidate_profile.user_id <> current_profile.user_id
                JOIN users candidate_user
                  ON candidate_user.id = candidate_profile.user_id
                 AND candidate_user.role = 'USER'
                 AND candidate_user.status = 'ACTIVE'
                LEFT JOIN schools candidate_school ON candidate_school.id = candidate_profile.school_id
                LEFT JOIN faculties candidate_faculty ON candidate_faculty.id = candidate_profile.faculty_id
                LEFT JOIN majors candidate_major ON candidate_major.id = candidate_profile.major_id
                WHERE current_profile.user_id = :currentUserId
                  AND candidate_profile.profile_completed_at IS NOT NULL
                  AND candidate_profile.username IS NOT NULL
                  AND candidate_profile.display_name IS NOT NULL
                  AND NOT EXISTS (
                      SELECT 1 FROM user_blocks blocked_pair
                      WHERE (blocked_pair.blocker_id = :currentUserId
                             AND blocked_pair.blocked_id = candidate_profile.user_id)
                         OR (blocked_pair.blocker_id = candidate_profile.user_id
                             AND blocked_pair.blocked_id = :currentUserId)
                  )
                  AND NOT EXISTS (
                      SELECT 1 FROM follows existing_follow
                      WHERE existing_follow.follower_id = :currentUserId
                        AND existing_follow.following_id = candidate_profile.user_id
                  )
            )
            SELECT COUNT(*)
            FROM recommendation_candidates
            WHERE sameSchool * 40
                  + sameFaculty * 25
                  + sameMajor * 20
                  + sameEntryYear * 10
                  + LEAST(commonInterestCount * 5, 25)
                  + LEAST(mutualConnectionCount * 3, 15) > 0
            """,
            nativeQuery = true)
    Page<StudentRecommendationProjection> findStudentRecommendations(
            @Param("currentUserId") Long currentUserId,
            Pageable pageable
    );
}
