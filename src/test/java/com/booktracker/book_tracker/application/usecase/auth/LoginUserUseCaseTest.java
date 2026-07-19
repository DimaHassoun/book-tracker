package com.booktracker.book_tracker.application.usecase.auth;

import com.booktracker.book_tracker.domain.exception.InvalidCredentialsException;
import com.booktracker.book_tracker.domain.model.User;
import com.booktracker.book_tracker.domain.repository.UserRepository;
import com.booktracker.book_tracker.infrastructure.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoginUserUseCaseTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    private LoginUserUseCase useCase;

    private User user;

    @BeforeEach
    void setUp() {
        useCase = new LoginUserUseCase(userRepository, passwordEncoder, jwtService);
        user = new User();
        user.setId(UUID.randomUUID());
        user.setUsername("dima");
        user.setEmail("dima@example.com");
        user.setPasswordHash("hashed-password");
    }

    @Test
    void execute_withValidUsernameAndPassword_shouldReturnAuthResult() {
        when(userRepository.findByUsername("dima")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("plaintext", "hashed-password")).thenReturn(true);
        when(jwtService.generateToken(user.getId(), "dima")).thenReturn("jwt-token");

        AuthResult result = useCase.execute("dima", "plaintext");

        assertThat(result.getUser()).isSameAs(user);
        assertThat(result.getToken()).isEqualTo("jwt-token");
        verify(userRepository, never()).findByEmail(any());
    }

    @Test
    void execute_withValidEmail_shouldFallBackToEmailLookup() {
        when(userRepository.findByUsername("dima@example.com")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("dima@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("plaintext", "hashed-password")).thenReturn(true);
        when(jwtService.generateToken(user.getId(), "dima")).thenReturn("jwt-token");

        AuthResult result = useCase.execute("dima@example.com", "plaintext");

        assertThat(result.getUser()).isSameAs(user);
    }

    @Test
    void execute_whenUserNotFoundByUsernameOrEmail_shouldThrowInvalidCredentialsException() {
        when(userRepository.findByUsername("ghost")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("ghost")).thenReturn(Optional.empty());

        assertThrows(InvalidCredentialsException.class, () -> useCase.execute("ghost", "whatever"));

        verify(jwtService, never()).generateToken(any(), any());
    }

    @Test
    void execute_whenPasswordWrong_shouldThrowInvalidCredentialsException() {
        when(userRepository.findByUsername("dima")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong-password", "hashed-password")).thenReturn(false);

        assertThrows(InvalidCredentialsException.class, () -> useCase.execute("dima", "wrong-password"));

        verify(jwtService, never()).generateToken(any(), any());
    }
}
