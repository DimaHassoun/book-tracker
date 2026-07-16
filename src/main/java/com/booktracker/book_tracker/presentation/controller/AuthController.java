package com.booktracker.book_tracker.presentation.controller;

import com.booktracker.book_tracker.application.usecase.auth.AuthResult;
import com.booktracker.book_tracker.application.usecase.auth.LoginUserUseCase;
import com.booktracker.book_tracker.application.usecase.auth.RegisterUserUseCase;
import com.booktracker.book_tracker.domain.model.User;
import com.booktracker.book_tracker.presentation.dto.request.LoginRequest;
import com.booktracker.book_tracker.presentation.dto.request.RegisterRequest;
import com.booktracker.book_tracker.presentation.dto.response.AuthResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final RegisterUserUseCase registerUserUseCase;
    private final LoginUserUseCase loginUserUseCase;

    public AuthController(RegisterUserUseCase registerUserUseCase, LoginUserUseCase loginUserUseCase) {
        this.registerUserUseCase = registerUserUseCase;
        this.loginUserUseCase = loginUserUseCase;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        registerUserUseCase.execute(request.getUsername(), request.getEmail(), request.getPassword());
        AuthResult result = loginUserUseCase.execute(request.getUsername(), request.getPassword());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new AuthResponse(result.getUser().getId(), result.getUser().getUsername(), result.getToken()));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResult result = loginUserUseCase.execute(request.getUsernameOrEmail(), request.getPassword());
        return ResponseEntity.ok(new AuthResponse(result.getUser().getId(), result.getUser().getUsername(), result.getToken()));
    }
}