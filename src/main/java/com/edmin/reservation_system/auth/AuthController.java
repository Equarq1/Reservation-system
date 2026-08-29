package com.edmin.reservation_system.auth;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/auth")
public class AuthorizationController {

    private final AuthService authService;

    public AuthorizationController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest dto) {
        return ResponseEntity.ok(new AuthResponse("Успешный вход"));
    }

    @PostMapping("/registration")
    public ResponseEntity<AuthResponse> registration(@Valid @RequestBody RegistrationRequest dto) {
        authService.register(dto);
        return ResponseEntity.status(201).body(new AuthResponse("Успешная регистрация"));
    }
}
