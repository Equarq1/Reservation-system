package com.edmin.reservation_system.auth;

public record LoginRequest(
        String email,
        String password
) {
}
