package ru.vortex.geocoder.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.vortex.geocoder.dto.AuthResponseDto;
import ru.vortex.geocoder.dto.LoginRequestDto;
import ru.vortex.geocoder.dto.RegisterRequestDto;
import ru.vortex.geocoder.service.AuthService;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Авторизация", description = "Регистрация, вход и refresh")
@CrossOrigin(origins = "*")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @Operation(summary = "Регистрация нового пользователя")
    @PostMapping("/register")
    public ResponseEntity<AuthResponseDto> register(@RequestBody @Valid RegisterRequestDto request) {
        AuthResponseDto tokens = authService.register(request);
        return ResponseEntity.ok(tokens);
    }

    @Operation(summary = "Вход в систему")
    @PostMapping("/login")
    public ResponseEntity<AuthResponseDto> login(@RequestBody @Valid LoginRequestDto request) {
        AuthResponseDto tokens = authService.login(request.getUsername(), request.getPassword());
        return ResponseEntity.ok(tokens);
    }

    @Operation(summary = "Обновить access-токен по refresh-токену")
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponseDto> refresh(@RequestBody RefreshRequestDto request) {
        AuthResponseDto tokens = authService.refresh(request.getRefreshToken());
        return ResponseEntity.ok(tokens);
    }
}

class RefreshRequestDto {
    private String refreshToken;
    public String getRefreshToken() { return refreshToken; }
    public void setRefreshToken(String refreshToken) { this.refreshToken = refreshToken; }
}