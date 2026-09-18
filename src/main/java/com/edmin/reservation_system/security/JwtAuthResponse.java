package com.edmin.reservation_system.security;

public record JwtAuthResponse(
        String accessToken,
        String refreshToken
) {}
