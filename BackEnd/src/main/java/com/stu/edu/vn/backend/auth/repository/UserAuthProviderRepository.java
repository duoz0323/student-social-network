package com.stu.edu.vn.backend.auth.repository;

import com.stu.edu.vn.backend.auth.entity.UserAuthProvider;
import com.stu.edu.vn.backend.auth.enums.AuthProvider;
import java.util.Optional;
import java.util.List;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/** Repository tra cứu các phương thức social đã liên kết. */
public interface UserAuthProviderRepository extends JpaRepository<UserAuthProvider, Long> {

    Optional<UserAuthProvider> findByProviderAndProviderUserId(AuthProvider provider, String providerUserId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select providerLink from UserAuthProvider providerLink "
            + "where providerLink.provider = :provider and providerLink.providerUserId = :providerUserId")
    Optional<UserAuthProvider> findByProviderAndProviderUserIdForUpdate(
            @Param("provider") AuthProvider provider,
            @Param("providerUserId") String providerUserId
    );

    Optional<UserAuthProvider> findByUserIdAndProvider(Long userId, AuthProvider provider);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select providerLink from UserAuthProvider providerLink "
            + "where providerLink.user.id = :userId and providerLink.provider = :provider")
    Optional<UserAuthProvider> findByUserIdAndProviderForUpdate(
            @Param("userId") Long userId,
            @Param("provider") AuthProvider provider
    );

    boolean existsByProviderAndProviderUserId(AuthProvider provider, String providerUserId);

    boolean existsByUserIdAndProvider(Long userId, AuthProvider provider);

    List<UserAuthProvider> findAllByUserIdOrderByProviderAsc(Long userId);
}
