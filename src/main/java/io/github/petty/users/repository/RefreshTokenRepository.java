package io.github.petty.users.repository;

import io.github.petty.users.entity.RefreshToken;
import io.github.petty.users.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {

    // 특정 사용자의 활성화된 리프레시 토큰 조회
    @Query("SELECT r FROM RefreshToken r WHERE r.user.provider = :provider AND r.user.username = :username AND r.used = false ORDER BY r.createdAt DESC")
    List<RefreshToken> findActiveTokensByProviderAndUsername(@Param("provider") String provider, @Param("username") String username);

    // 사용자의 유효한 활성 토큰 조회
    @Query("SELECT r FROM RefreshToken r WHERE r.user = :user AND r.used = false AND r.expiredAt > :now ORDER BY r.createdAt ASC")
    List<RefreshToken> findActiveTokensByUser(@Param("user") Users user, @Param("now") LocalDateTime now);

    // ID로 사용되지 않은 토큰 조회
    Optional<RefreshToken> findByIdAndUsedIsFalse(UUID id);
}
