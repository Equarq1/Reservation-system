package com.edmin.reservation_system.auth;

import com.edmin.reservation_system.security.*;
import com.edmin.reservation_system.users.UserEntity;
import com.edmin.reservation_system.users.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    private final RefreshTokenService refreshTokenService;
    private final RefreshTokenRepository refreshTokenRepository;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, AuthenticationManager authenticationManager, JwtUtils jwtUtils, RefreshTokenService refreshTokenService, RefreshTokenRepository refreshTokenRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtUtils = jwtUtils;
        this.refreshTokenService = refreshTokenService;
        this.refreshTokenRepository = refreshTokenRepository;
    }

    @Transactional
    public JwtAuthResponse register(RegistrationRequest dto) {
        if (!dto.password().equals(dto.passwordAgain())) {
            throw new IllegalArgumentException("Пароли не совпадают");
        }

        if (userRepository.existsByEmail(dto.email())) {
            throw new IllegalArgumentException("Пользователь с таким email уже существует");
        }

        String encodePassword = passwordEncoder.encode(dto.password());
        UserEntity userEntity = new UserEntity(dto.email(), encodePassword, "ROLE_USER");
        userRepository.save(userEntity);
        String accessToken = jwtUtils.generateToken(dto.email());
        RefreshTokenEntity refreshToken = refreshTokenService.createRefreshToken(userEntity.getId());
        return new JwtAuthResponse(accessToken, refreshToken.getToken());
    }
    @Transactional
    public JwtAuthResponse login(LoginRequest dto) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(dto.email(), dto.password()));
        UserEntity user = userRepository.findByEmail(dto.email()).orElseThrow(() -> new RuntimeException("User not found"));
        String token = jwtUtils.generateToken(user.getEmail());
        RefreshTokenEntity refreshToken = refreshTokenService.createRefreshToken(user.getId());
        return new JwtAuthResponse(token, refreshToken.getToken());
    }


    @Transactional
    public JwtAuthResponse refreshAccessToken(RefreshTokenRequest request) {
        return refreshTokenRepository.findByToken(request.refreshToken())
                .map(refreshTokenService::verifyExpiration)
                .map(RefreshTokenEntity::getUser)
                .map(user -> {
                    String newAccessToken = jwtUtils.generateToken(user.getEmail());
                    RefreshTokenEntity newRefreshToken = refreshTokenService.createRefreshToken(user.getId());
                    return new JwtAuthResponse(newAccessToken, newRefreshToken.getToken());
                })
                .orElseThrow(() -> new RuntimeException("Refresh token is not in database!"));
    }

    @Transactional
    public void logout(Long userId) {
        refreshTokenService.deleteByUserId(userId);
    }

}
