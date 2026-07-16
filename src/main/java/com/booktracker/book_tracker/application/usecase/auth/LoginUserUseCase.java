package com.booktracker.book_tracker.application.usecase.auth;

import com.booktracker.book_tracker.domain.exception.InvalidCredentialsException;
import com.booktracker.book_tracker.domain.model.User;
import com.booktracker.book_tracker.domain.repository.UserRepository;
import com.booktracker.book_tracker.infrastructure.security.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class LoginUserUseCase {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public LoginUserUseCase(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public AuthResult execute(String usernameOrEmail, String rawPassword) {
            User user = userRepository.findByUsername(usernameOrEmail)
                    .or(() -> userRepository.findByEmail(usernameOrEmail))
                    .orElseThrow(() -> new InvalidCredentialsException("Invalid username/email or password"));

            if (!passwordEncoder.matches(rawPassword, user.getPasswordHash())) {
                throw new InvalidCredentialsException("Invalid username/email or password");
            }

            String token = jwtService.generateToken(user.getId(), user.getUsername());
            return new AuthResult(user, token);
    }
}