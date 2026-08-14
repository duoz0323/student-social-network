package com.stu.edu.vn.backend.database;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;

/** Bảo vệ hai artifact database canonical và dữ liệu demo 1.000 tài khoản trong file import duy nhất. */
class DemoSeedContractTest {
    private static final double BINH_TRI_DONG_LATITUDE = 10.7586500d;
    private static final double BINH_TRI_DONG_LONGITUDE = 106.6048500d;
    private static final Pattern BINH_TRI_DONG_LOCATION = Pattern.compile(
            "\\('demo-binhtri-[^']+',\\s*'[^']+',\\s*'[^']+',\\s*([0-9.]+),\\s*([0-9.]+)\\)");

    @Test
    void databaseDirectoryKeepsOnlyCanonicalSqlAndDbml() throws Exception {
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

        assertThat(files).containsExactly(
                "student_social_network.dbml",
                "student_social_network.sql");
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
    void demoSeedProvidesPublishedNearbyPostsAroundBinhTriDong() throws Exception {
        String seed = Files.readString(databaseDirectory().resolve("student_social_network.sql"));
        List<double[]> coordinates = extractBinhTriDongCoordinates(seed);

        // Các điểm demo phải thật sự nằm trong bán kính 5 km từ Chợ Bình Trị Đông để manual E2E có dữ liệu.
        assertThat(coordinates).hasSize(5);
        assertThat(coordinates)
                .allSatisfy(coordinate -> assertThat(distanceKm(
                        BINH_TRI_DONG_LATITUDE,
                        BINH_TRI_DONG_LONGITUDE,
                        coordinate[0],
                        coordinate[1])).isLessThanOrEqualTo(5.0d));

        assertThat(seed)
                .contains("WHEN 902 THEN (SELECT id FROM `locations` WHERE `google_place_id` = 'demo-binhtri-aeon-mall')")
                .contains("WHEN 907 THEN (SELECT id FROM `locations` WHERE `google_place_id` = 'demo-binhtri-tan-khai-temple')")
                .contains("WHEN 902 THEN 'Cuối tuần ghé AEON Mall Bình Tân học nhóm")
                .contains("WHEN 907 THEN 'Một góc kiến trúc và không gian yên tĩnh ở Đình Tân Khai")
                .contains("'invalid_binh_tri_dong_nearby_seed'");
    }

    private List<double[]> extractBinhTriDongCoordinates(String seed) {
        Matcher matcher = BINH_TRI_DONG_LOCATION.matcher(seed);
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
