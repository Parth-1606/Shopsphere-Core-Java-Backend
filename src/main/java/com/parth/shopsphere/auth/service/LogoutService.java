package com.parth.shopsphere.auth.service;

import com.parth.shopsphere.security.JwtService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LogoutService implements LogoutHandler {

    private final TokenBlacklistService tokenBlacklistService;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final com.parth.shopsphere.user.repository.UserRepository userRepository;

    @Override
    public void logout(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) {
        final String authHeader = request.getHeader("Authorization");
        final String jwt;

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return;
        }

        jwt = authHeader.substring(7);
        try {
            String userEmail = jwtService.extractUsername(jwt);
            if (userEmail != null) {
                userRepository.findByEmail(userEmail).ifPresent(refreshTokenService::revokeAllUserTokens);
                long expirationTime = jwtService.getExpirationTime(jwt);
                long ttl = expirationTime - System.currentTimeMillis();
                if (ttl > 0) {
                    tokenBlacklistService.blacklistToken(jwt, ttl);
                }
            }
        } catch (Exception e) {
            // Token might be already expired or invalid
        }
    }
}
