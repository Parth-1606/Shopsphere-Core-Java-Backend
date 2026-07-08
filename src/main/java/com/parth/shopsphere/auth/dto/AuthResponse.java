package com.parth.shopsphere.auth.dto;

import lombok.Builder;

@Builder
public record AuthResponse(
        String token,
        String refreshToken,
        String email,
        String firstName,
        String lastName,
        String role
) {}
