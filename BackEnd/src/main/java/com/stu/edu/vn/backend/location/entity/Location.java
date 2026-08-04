package com.stu.edu.vn.backend.location.entity;

import com.stu.edu.vn.backend.common.entity.BaseAuditEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.math.BigDecimal;

/**
 * Entity locations lưu thông tin địa điểm Google Places dùng chung giữa nhiều bài viết.
 */
@Entity
@Table(
        name = "locations",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_locations_google_place_id",
                columnNames = "google_place_id"
        )
)
public class Location extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    // Khóa chính nội bộ tự tăng; Google Place ID là khóa tự nhiên dùng để chống trùng.
    private Long id;

    @Column(name = "google_place_id", nullable = false, length = 255)
    // Định danh bất biến do Google Places cung cấp và là natural unique key duy nhất trong phạm vi P1.
    private String googlePlaceId;

    @Column(name = "display_name", nullable = false, length = 255)
    // Tên địa điểm đã được Backend chuẩn hóa trước khi lưu ở giai đoạn tích hợp nghiệp vụ.
    private String displayName;

    @Column(name = "formatted_address", length = 500)
    // Địa chỉ hiển thị là tùy chọn vì Google Places có thể không trả đầy đủ cho mọi địa điểm.
    private String formattedAddress;

    @Column(name = "latitude", nullable = false, precision = 10, scale = 7)
    // Vĩ độ dùng BigDecimal để giữ đúng độ chính xác của schema DECIMAL(10,7).
    private BigDecimal latitude;

    @Column(name = "longitude", nullable = false, precision = 10, scale = 7)
    // Kinh độ dùng BigDecimal để tránh sai số nhị phân của double hoặc float.
    private BigDecimal longitude;

    protected Location() {
        // Constructor rỗng dành cho JPA.
    }

    public Location(
            String googlePlaceId,
            String displayName,
            String formattedAddress,
            BigDecimal latitude,
            BigDecimal longitude
    ) {
        // Constructor tối thiểu nhận dữ liệu Location đã được validate và chuẩn hóa.
        this.googlePlaceId = googlePlaceId;
        this.displayName = displayName;
        this.formattedAddress = formattedAddress;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public Long getId() {
        return id;
    }

    public String getGooglePlaceId() {
        return googlePlaceId;
    }

    public void setGooglePlaceId(String googlePlaceId) {
        this.googlePlaceId = googlePlaceId;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getFormattedAddress() {
        return formattedAddress;
    }

    public void setFormattedAddress(String formattedAddress) {
        this.formattedAddress = formattedAddress;
    }

    public BigDecimal getLatitude() {
        return latitude;
    }

    public void setLatitude(BigDecimal latitude) {
        this.latitude = latitude;
    }

    public BigDecimal getLongitude() {
        return longitude;
    }

    public void setLongitude(BigDecimal longitude) {
        this.longitude = longitude;
    }
}
