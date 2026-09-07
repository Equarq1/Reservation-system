package com.edmin.reservation_system.auth;

import com.edmin.reservation_system.security.JwtUtils;
import com.edmin.reservation_system.users.UserEntity;
import com.edmin.reservation_system.users.UserRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, AuthenticationManager authenticationManager, JwtUtils jwtUtils) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtUtils = jwtUtils;
    }


    public AuthResponse register(RegistrationRequest dto) {
        if (!dto.password().equals(dto.passwordAgain())) {
            throw new IllegalArgumentException("Пароли не совпадают");
        }

        if (userRepository.existsByEmail(dto.email())) {
            throw new IllegalArgumentException("Пользователь с таким email уже существует");
        }

        String encodePassword = passwordEncoder.encode(dto.password());
        UserEntity userEntity = new UserEntity(dto.email(), encodePassword, "ROLE_USER");
        userRepository.save(userEntity);
        String token = jwtUtils.generateToken(dto.email());
        return new AuthResponse(token);
    }

    public AuthResponse login(LoginRequest dto) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(dto.email(), dto.password()));
        String token = jwtUtils.generateToken(dto.email());
        return new AuthResponse(token);
    }


}
