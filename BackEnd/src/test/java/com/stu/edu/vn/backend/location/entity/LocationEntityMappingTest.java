package com.stu.edu.vn.backend.location.entity;

import static org.assertj.core.api.Assertions.assertThat;

import com.stu.edu.vn.backend.common.entity.BaseAuditEntity;
import jakarta.persistence.Column;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collection;
import org.junit.jupiter.api.Test;

class LocationEntityMappingTest {

    @Test
    void locationMappingMatchesLocationsTable() throws Exception {
        // Metadata JPA phải khớp schema để ddl-auto=validate phát hiện sai lệch tên và nullable.
        assertThat(BaseAuditEntity.class).isAssignableFrom(Location.class);
        Table table = Location.class.getAnnotation(Table.class);
        assertThat(table.name()).isEqualTo("locations");
        assertThat(table.uniqueConstraints())
                .extracting(UniqueConstraint::name)
                .containsExactly("uk_locations_google_place_id");
        assertThat(table.uniqueConstraints()[0].columnNames()).containsExactly("google_place_id");

        Column googlePlaceId = column("googlePlaceId");
        assertThat(googlePlaceId.name()).isEqualTo("google_place_id");
        assertThat(googlePlaceId.nullable()).isFalse();
        assertThat(googlePlaceId.length()).isEqualTo(255);

        Column displayName = column("displayName");
        assertThat(displayName.name()).isEqualTo("display_name");
        assertThat(displayName.nullable()).isFalse();
        assertThat(displayName.length()).isEqualTo(255);

        Column formattedAddress = column("formattedAddress");
        assertThat(formattedAddress.name()).isEqualTo("formatted_address");
        assertThat(formattedAddress.nullable()).isTrue();
        assertThat(formattedAddress.length()).isEqualTo(500);
    }

    @Test
    void coordinatesUseBigDecimalWithRequiredPrecisionAndScale() throws Exception {
        // Tọa độ dùng DECIMAL(10,7), không dùng kiểu dấu phẩy động nhị phân.
        assertThat(Location.class.getDeclaredField("latitude").getType()).isEqualTo(BigDecimal.class);
        assertThat(Location.class.getDeclaredField("longitude").getType()).isEqualTo(BigDecimal.class);

        Column latitude = column("latitude");
        assertThat(latitude.nullable()).isFalse();
        assertThat(latitude.precision()).isEqualTo(10);
        assertThat(latitude.scale()).isEqualTo(7);

        Column longitude = column("longitude");
        assertThat(longitude.nullable()).isFalse();
        assertThat(longitude.precision()).isEqualTo(10);
        assertThat(longitude.scale()).isEqualTo(7);
    }

    @Test
    void locationDoesNotExposePostsCollection() {
        // Mapping một chiều tránh tải collection Post lớn và ngăn vòng lặp serialization không cần thiết.
        assertThat(Arrays.stream(Location.class.getDeclaredFields()))
                .noneMatch(field -> field.getAnnotation(OneToMany.class) != null);
        assertThat(Arrays.stream(Location.class.getDeclaredFields()))
                .noneMatch(field -> Collection.class.isAssignableFrom(field.getType()));
    }

    private Column column(String fieldName) throws NoSuchFieldException {
        return Location.class.getDeclaredField(fieldName).getAnnotation(Column.class);
    }
}
