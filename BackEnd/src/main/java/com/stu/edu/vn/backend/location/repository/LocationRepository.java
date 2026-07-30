package com.stu.edu.vn.backend.location.repository;

import com.stu.edu.vn.backend.location.entity.Location;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Repository truy vấn Location theo Google Place ID để các bài viết tái sử dụng cùng một bản ghi.
 */
public interface LocationRepository extends JpaRepository<Location, Long> {

    // Tìm Location theo natural unique key; không dùng tên hoặc tọa độ để xác định trùng.
    Optional<Location> findByGooglePlaceId(String googlePlaceId);

    // Kiểm tra nhanh Google Place ID đã tồn tại trước khi thực hiện nghiệp vụ ở giai đoạn tiếp theo.
    boolean existsByGooglePlaceId(String googlePlaceId);

    /**
     * Insert nguyên tử theo unique Google Place ID; nhánh duplicate chỉ tự gán khóa tự nhiên và không sửa metadata cũ.
     */
    @Modifying
    @Query(
            value = """
                    INSERT INTO locations (
                        google_place_id, display_name, formatted_address, latitude, longitude
                    )
                    VALUES (
                        :googlePlaceId, :displayName, :formattedAddress, :latitude, :longitude
                    )
                    ON DUPLICATE KEY UPDATE google_place_id = google_place_id
                    """,
            nativeQuery = true
    )
    void insertIfAbsent(
            @Param("googlePlaceId") String googlePlaceId,
            @Param("displayName") String displayName,
            @Param("formattedAddress") String formattedAddress,
            @Param("latitude") java.math.BigDecimal latitude,
            @Param("longitude") java.math.BigDecimal longitude
    );
}
