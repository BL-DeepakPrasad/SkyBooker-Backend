package com.skybooker.auth.controller;

import com.skybooker.auth.dto.*;
import com.skybooker.auth.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<UserDto> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestHeader("Authorization") String token) {
        authService.logout(token);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@RequestHeader("Authorization") String token) {
        return ResponseEntity.ok(authService.refreshToken(token));
    }

    @GetMapping("/profile/{userId}")
    public ResponseEntity<UserDto> getProfile(@PathVariable int userId) {
        return ResponseEntity.ok(authService.getUserById(userId));
    }

    @PutMapping("/profile/{userId}")
    public ResponseEntity<UserDto> updateProfile(
            @PathVariable int userId,
            @Valid @RequestBody UserDto user) {

        return ResponseEntity.ok(
                authService.updateProfile(userId, user)
        );
    }

    @PutMapping("/change-password/{userId}")
    public ResponseEntity<Void> changePassword(
            @PathVariable int userId,
            @RequestBody Map<String, String> request) {

        authService.changePassword(
                userId,
                request.get("newPassword")
        );

        return ResponseEntity.ok().build();
    }

    @PutMapping("/deactivate/{userId}")
    public ResponseEntity<Void> deactivateAccount(
            @PathVariable int userId) {

        authService.deactivateAccount(userId);
        return ResponseEntity.ok().build();
    }
}
