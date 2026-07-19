package com.booktracker.book_tracker.application.usecase.auth;

import com.booktracker.book_tracker.domain.exception.DuplicateUserException;
import com.booktracker.book_tracker.domain.model.User;
import com.booktracker.book_tracker.domain.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RegisterUserUseCaseTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private RegisterUserUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new RegisterUserUseCase(userRepository, passwordEncoder);
    }

    @Test
    void execute_withNewUsernameAndEmail_shouldEncodePasswordAndSaveUser() {
        when(userRepository.existsByUsername("dima")).thenReturn(false);
        when(userRepository.existsByEmail("dima@example.com")).thenReturn(false);
        when(passwordEncoder.encode("plaintext")).thenReturn("hashed-password");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        User result = useCase.execute("dima", "dima@example.com", "plaintext");

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        User saved = captor.getValue();

        assertThat(saved.getUsername()).isEqualTo("dima");
        assertThat(saved.getEmail()).isEqualTo("dima@example.com");
        assertThat(saved.getPasswordHash()).isEqualTo("hashed-password");
        assertThat(saved.isPrivate()).isTrue();
        assertThat(saved.getTimezone()).isEqualTo("UTC");
        assertThat(result).isSameAs(saved);
    }

    @Test
    void execute_whenUsernameTaken_shouldThrowDuplicateUserExceptionAndNeverCheckEmail() {
        when(userRepository.existsByUsername("dima")).thenReturn(true);

        assertThrows(DuplicateUserException.class,
                () -> useCase.execute("dima", "dima@example.com", "plaintext"));

        verify(userRepository, never()).existsByEmail(any());
        verify(userRepository, never()).save(any());
    }

    @Test
    void execute_whenEmailTaken_shouldThrowDuplicateUserException() {
        when(userRepository.existsByUsername("dima")).thenReturn(false);
        when(userRepository.existsByEmail("dima@example.com")).thenReturn(true);

        assertThrows(DuplicateUserException.class,
                () -> useCase.execute("dima", "dima@example.com", "plaintext"));

        verify(userRepository, never()).save(any());
    }
}
