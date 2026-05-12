package com.studycoachai.service;

import com.studycoachai.dto.AuthLoginRequest;
import com.studycoachai.dto.AuthRegisterRequest;
import com.studycoachai.dto.AuthResponse;
import com.studycoachai.dto.UserResponse;
import com.studycoachai.entity.User;
import com.studycoachai.exception.ResourceNotFoundException;
import com.studycoachai.repository.UserRepository;
import com.studycoachai.security.JwtService;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional
    public AuthResponse register(AuthRegisterRequest request) {
        if (userRepository.existsByUsername(request.username())) {
            throw new IllegalArgumentException("Username is already used.");
        }
        if (userRepository.existsByEmail(request.email())) {
            throw new IllegalArgumentException("Email is already used.");
        }
        User user;
        try {
            user = userRepository.saveAndFlush(new User(
                    request.username(),
                    request.email(),
                    passwordEncoder.encode(request.password())
            ));
        } catch (DataIntegrityViolationException exception) {
            throw new IllegalArgumentException("Username or email is already used.");
        }
        return toAuthResponse(user);
    }

    @Transactional(readOnly = true)
    public AuthResponse login(AuthLoginRequest request) {
        User user = userRepository.findByUsername(request.usernameOrEmail())
                .or(() -> userRepository.findByEmail(request.usernameOrEmail()))
                .orElseThrow(() -> new IllegalArgumentException("Invalid username/email or password."));
        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new IllegalArgumentException("Invalid username/email or password.");
        }
        return toAuthResponse(user);
    }

    @Transactional(readOnly = true)
    public UserResponse me(Long userId) {
        return UserResponse.from(userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId)));
    }

    private AuthResponse toAuthResponse(User user) {
        return new AuthResponse(jwtService.generateToken(user), UserResponse.from(user));
    }
}
