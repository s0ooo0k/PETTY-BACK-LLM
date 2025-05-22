package io.github.petty.users.service;

import io.github.petty.users.dto.RefreshTokenResponseDTO;
import io.github.petty.users.entity.RefreshToken;
import io.github.petty.users.entity.Users;
import io.github.petty.users.jwt.JWTUtil;
import io.github.petty.users.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {
    private static final Logger logger = LoggerFactory.getLogger(RefreshTokenService.class);
    private final RefreshTokenRepository refreshTokenRepository;
    private final JWTUtil jwtUtil;

    // 새 리프레시 토큰 생성
    @Transactional
    public UUID createRefreshToken(Users user) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiresAt = now.plusDays(7); // 리프레시 토큰 7일 유효

        // 활성 토큰 관리 (최대 3개로 제한)
        List<RefreshToken> activeTokens = refreshTokenRepository
                .findActiveTokensByUser(user, now);

        final int MAX_TOKENS_PER_USER = 3;
        if (activeTokens.size() >= MAX_TOKENS_PER_USER) {
            // 가장 오래된 토큰부터 삭제
            for (int i = 0; i < activeTokens.size() - (MAX_TOKENS_PER_USER - 1); i++) {
                refreshTokenRepository.delete(activeTokens.get(i));
            }
        }

        // 새 리프레시 토큰 생성
        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .provider(user.getProvider())
                .expiredAt(expiresAt)
                .used(false)
                .build();

        RefreshToken savedToken = refreshTokenRepository.save(refreshToken);
        logger.debug("리프레시 토큰 생성: 사용자={}, 토큰={}", user.getUsername(), savedToken.getId());
        return savedToken.getId();
    }

    // 액세스 토큰 및 리프레시 토큰 갱신
    @Transactional
    public RefreshTokenResponseDTO refreshAccessToken(UUID refreshTokenId) {
        RefreshToken refreshToken = refreshTokenRepository.findByIdAndUsedIsFalse(refreshTokenId)
                .orElseThrow(() -> new RuntimeException("유효하지 않은 리프레시 토큰입니다."));

        // 토큰 만료 체크
        if (refreshToken.isExpired()) {
            refreshTokenRepository.invalidateAllUserTokens(refreshToken.getUser());
            logger.warn("만료된 리프레시 토큰: 사용자={}", refreshToken.getUser().getUsername());
            throw new RuntimeException("만료된 리프레시 토큰입니다.");
        }

        // 토큰 사용 처리 (RTR 핵심)
        refreshToken.markUsed();
        refreshTokenRepository.markAsUsed(refreshToken.getId());

        // 새 액세스 토큰 생성
        Users user = refreshToken.getUser();
        String newAccessToken = jwtUtil.createJwt(
                user.getUsername(),
                user.getRole(),
                3600000L); // 1시간

        // 새 리프레시 토큰 생성
        UUID newRefreshToken = createRefreshToken(user);

        logger.debug("토큰 리프레시 성공: 사용자={}", user.getUsername());
        return new RefreshTokenResponseDTO(newAccessToken, newRefreshToken.toString());
    }

    // 사용자의 모든 리프레시 토큰 무효화
    @Transactional
    public void invalidateUserTokens(Users user) {
        refreshTokenRepository.invalidateAllUserTokens(user);
        logger.debug("사용자 토큰 전체 무효화: 사용자={}", user.getUsername());
    }
}