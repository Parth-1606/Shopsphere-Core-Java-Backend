package com.parth.shopsphere.auth.service;

import com.parth.shopsphere.auth.dto.AuthResponse;
import com.parth.shopsphere.auth.dto.LoginRequest;
import com.parth.shopsphere.auth.dto.RegisterRequest;
import com.parth.shopsphere.auth.dto.RefreshTokenRequest;
import com.parth.shopsphere.auth.entity.RefreshToken;
import com.parth.shopsphere.common.exception.BadRequestException;
import com.parth.shopsphere.security.CustomUserDetails;
import com.parth.shopsphere.security.JwtService;
import com.parth.shopsphere.user.entity.User;
import com.parth.shopsphere.user.enums.Role;
import com.parth.shopsphere.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final RefreshTokenService refreshTokenService;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new BadRequestException("Email is already in use.");
        }

        User user = User.builder()
                .firstName(request.firstName())
                .lastName(request.lastName())
                .email(request.email())
                .passwordHash(passwordEncoder.encode(request.password()))
                .phone(request.phone())
                .role(Role.ROLE_USER)
                .isActive(true)
                .build();

        userRepository.save(user);

        CustomUserDetails userDetails = new CustomUserDetails(user);
        String jwtToken = jwtService.generateToken(userDetails);
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);

        return AuthResponse.builder()
                .token(jwtToken)
                .refreshToken(refreshToken.getToken())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .role(user.getRole().name())
                .build();
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.email(),
                        request.password()
                )
        );

        User user = userRepository.findByEmail(request.email())
                .orElseThrow(); // AuthenticationManager already verified the user exists and credentials are correct

        CustomUserDetails userDetails = new CustomUserDetails(user);
        String jwtToken = jwtService.generateToken(userDetails);
        
        refreshTokenService.revokeAllUserTokens(user);
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);

        return AuthResponse.builder()
                .token(jwtToken)
                .refreshToken(refreshToken.getToken())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .role(user.getRole().name())
                .build();
    }

    @Transactional
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        return refreshTokenService.findByToken(request.refreshToken())
                .map(refreshTokenService::verifyExpiration)
                .map(RefreshToken::getUser)
                .map(user -> {
                    CustomUserDetails userDetails = new CustomUserDetails(user);
                    String jwtToken = jwtService.generateToken(userDetails);
                    return AuthResponse.builder()
                            .token(jwtToken)
                            .refreshToken(request.refreshToken())
                            .email(user.getEmail())
                            .firstName(user.getFirstName())
                            .lastName(user.getLastName())
                            .role(user.getRole().name())
                            .build();
                }).orElseThrow(() -> new BadRequestException("Refresh token is not in database!"));
    }
}
