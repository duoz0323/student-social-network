package com.stu.edu.vn.backend.database;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;

/** Bảo vệ artifact database canonical, migration thủ công và dữ liệu demo 1.000 tài khoản. */
class DemoSeedContractTest {
    private static final double CAO_LO_LATITUDE = 10.7387550d;
    private static final double CAO_LO_LONGITUDE = 106.6777880d;
    private static final Pattern CAO_LO_LOCATION = Pattern.compile(
            "\\('demo-caolo-[^']+',\\s*'[^']+',\\s*'[^']+',\\s*([0-9.]+),\\s*([0-9.]+)\\)");

    @Test
    void databaseDirectoryKeepsCanonicalArtifactsAndVersionedMigrations() throws Exception {
        Path database = databaseDirectory();
        List<String> files;
        try (var paths = Files.walk(database)) {
            files = paths.filter(Files::isRegularFile)
                    .map(database::relativize)
                    .map(Path::toString)
                    .map(path -> path.replace('\\', '/'))
                    .sorted()
                    .toList();
        }

        // Hai artifact canonical luôn nằm ở thư mục gốc; migration hợp lệ nằm riêng trong migrations/.
        assertThat(files)
                .contains(
                "student_social_network.dbml",
                "student_social_network.sql",
                "migrations/20260815_post_share_v1.sql")
                .allMatch(path -> path.equals("student_social_network.dbml")
                        || path.equals("student_social_network.sql")
                        || path.matches("migrations/\\d{8}_[a-z0-9_]+\\.sql"));
    }

    @Test
    void demoSeedCoversUsersAcademicProfilesPostsAndSelfVerification() throws Exception {
        String seed = Files.readString(databaseDirectory().resolve("student_social_network.sql"));

        assertThat(seed)
                .contains("WHILE user_no <= 1000 DO")
                .contains("WHILE post_no <= 1000 DO")
                .contains("TRUNCATE TABLE `user_interests`")
                .contains("`school_id`, `faculty_id`, `major_id`, `entry_year`")
                .contains("INSERT INTO `user_interests`")
                .contains("'invalid_demo_counts'")
                .contains("'invalid_academic_hierarchy'")
                .contains("'counter_mismatch'");
    }

    @Test
    void demoSeedProvidesPublishedNearbyPostsAroundCaoLo() throws Exception {
        String seed = Files.readString(databaseDirectory().resolve("student_social_network.sql"));
        List<double[]> coordinates = extractCaoLoCoordinates(seed);

        // Các điểm demo phải nằm trong bán kính 5 km từ STU trên đường Cao Lỗ để manual E2E có dữ liệu.
        assertThat(coordinates).hasSize(5);
        assertThat(coordinates)
                .allSatisfy(coordinate -> assertThat(distanceKm(
                        CAO_LO_LATITUDE,
                        CAO_LO_LONGITUDE,
                        coordinate[0],
                        coordinate[1])).isLessThanOrEqualTo(5.0d));

        assertThat(seed)
                .contains("WHEN 902 THEN (SELECT id FROM `locations` WHERE `google_place_id` = 'demo-caolo-stu')")
                .contains("WHEN 911 THEN (SELECT id FROM `locations` WHERE `google_place_id` = 'demo-caolo-pham-the-hien-market')")
                .contains("WHEN 902 THEN 'Sáng nay học ở STU từ tiết một")
                .contains("WHEN 911 THEN 'Ai ở khu Cao Lỗ chưa biết ăn gì chiều nay")
                .contains("AND post.id BETWEEN 902 AND 911")
                .contains("AND post.status = 'PUBLISHED') <> 10")
                .contains("'invalid_cao_lo_nearby_seed'");
    }

    private List<double[]> extractCaoLoCoordinates(String seed) {
        Matcher matcher = CAO_LO_LOCATION.matcher(seed);
        List<double[]> coordinates = new ArrayList<>();
        while (matcher.find()) {
            coordinates.add(new double[]{
                    Double.parseDouble(matcher.group(1)),
                    Double.parseDouble(matcher.group(2))
            });
        }
        return coordinates;
    }

    private double distanceKm(double latitudeA, double longitudeA, double latitudeB, double longitudeB) {
        double latitudeDelta = Math.toRadians(latitudeB - latitudeA);
        double longitudeDelta = Math.toRadians(longitudeB - longitudeA);
        double haversine = Math.pow(Math.sin(latitudeDelta / 2), 2)
                + Math.cos(Math.toRadians(latitudeA)) * Math.cos(Math.toRadians(latitudeB))
                * Math.pow(Math.sin(longitudeDelta / 2), 2);
        return 6_371.0088d * 2 * Math.atan2(Math.sqrt(haversine), Math.sqrt(1 - haversine));
    }

    private Path databaseDirectory() {
        Path current = Path.of("").toAbsolutePath().normalize();
        return Files.isDirectory(current.resolve("database"))
                ? current.resolve("database")
                : current.resolve("..").resolve("database").normalize();
    }
}
