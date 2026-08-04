package com.stu.edu.vn.backend.location.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.stu.edu.vn.backend.location.entity.Location;
import com.stu.edu.vn.backend.location.repository.LocationRepository;
import com.stu.edu.vn.backend.post.dto.request.PostLocationRequest;
import com.stu.edu.vn.backend.post.validation.PostLocationValidator;
import java.math.BigDecimal;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class LocationResolverTest {
    private final LocationRepository repository = Mockito.mock(LocationRepository.class);
    private final LocationResolver resolver = new LocationResolver(repository, new PostLocationValidator());

    @Test
    void reusesExistingLocationWithoutOverwritingSharedMetadata() {
        Location existing = new Location("ChIJ-id", "Tên chuẩn", "Địa chỉ chuẩn",
                BigDecimal.TEN, BigDecimal.valueOf(106));
        when(repository.findByGooglePlaceId("ChIJ-id")).thenReturn(Optional.of(existing));

        Location result = resolver.resolve(request("Tên client"));

        assertThat(result).isSameAs(existing);
        assertThat(result.getDisplayName()).isEqualTo("Tên chuẩn");
        verify(repository, never()).insertIfAbsent(Mockito.any(), Mockito.any(), Mockito.any(),
                Mockito.any(), Mockito.any());
    }

    @Test
    void insertsAtomicallyThenReadsCanonicalRow() {
        Location inserted = new Location("ChIJ-id", "Tên client", null,
                BigDecimal.TEN, BigDecimal.valueOf(106));
        when(repository.findByGooglePlaceId("ChIJ-id"))
                .thenReturn(Optional.empty(), Optional.of(inserted));

        assertThat(resolver.resolve(request("Tên client"))).isSameAs(inserted);
        verify(repository).insertIfAbsent("ChIJ-id", "Tên client", null,
                BigDecimal.TEN, BigDecimal.valueOf(106));
    }

    private PostLocationRequest request(String name) {
        return new PostLocationRequest("ChIJ-id", name, null, BigDecimal.TEN, BigDecimal.valueOf(106));
    }
}
