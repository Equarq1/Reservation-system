package com.edmin.reservation_system.auth;

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

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
    }


    public void register(RegistrationRequest dto) {
        if (!dto.password().equals(dto.passwordAgain())) {
            throw new IllegalArgumentException("Пароли не совпадают");
        }

        if (userRepository.existsByEmail(dto.email())) {
            throw new IllegalArgumentException("Пользователь с таким email уже существует");
        }

        String encodePassword = passwordEncoder.encode(dto.password());
        UserEntity userEntity = new UserEntity(dto.email(), encodePassword, "ROLE_USER");
        userRepository.save(userEntity);
    }

    public AuthResponse login(LoginRequest dto) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(dto.email(), dto.password()));
        return new AuthResponse("Успешный вход");
    }


}
