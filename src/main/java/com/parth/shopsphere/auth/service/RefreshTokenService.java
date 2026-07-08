package com.parth.shopsphere.auth.service;

import com.parth.shopsphere.auth.entity.RefreshToken;
import com.parth.shopsphere.auth.repository.RefreshTokenRepository;
import com.parth.shopsphere.common.exception.BadRequestException;
import com.parth.shopsphere.config.JwtConfig;
import com.parth.shopsphere.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtConfig jwtConfig;

    @Transactional
    public RefreshToken createRefreshToken(User user) {
        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .token(UUID.randomUUID().toString())
                .expiryDate(Instant.now().plusMillis(jwtConfig.getRefreshExpirationMs()))
                .isRevoked(false)
                .build();

        return refreshTokenRepository.save(refreshToken);
    }

    public Optional<RefreshToken> findByToken(String token) {
        return refreshTokenRepository.findByToken(token);
    }

    public RefreshToken verifyExpiration(RefreshToken token) {
        if (token.getExpiryDate().compareTo(Instant.now()) < 0 || token.isRevoked()) {
            refreshTokenRepository.delete(token);
            throw new BadRequestException("Refresh token was expired or revoked. Please make a new signin request");
        }
        return token;
    }
    
    @Transactional
    public void revokeAllUserTokens(User user) {
        var validTokens = refreshTokenRepository.findAllByUserAndIsRevokedFalse(user);
        if (validTokens.isEmpty()) {
            return;
        }
        validTokens.forEach(token -> token.setRevoked(true));
        refreshTokenRepository.saveAll(validTokens);
    }
}
