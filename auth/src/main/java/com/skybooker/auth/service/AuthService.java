package com.skybooker.auth.service;

import com.skybooker.auth.dto.*;

import java.util.List;

public interface AuthService {

    UserDto register(RegisterRequest request);

    AuthResponse login(LoginRequest request);

    void logout(String token);

    boolean validateToken(String token);

    AuthResponse refreshToken(String token);

    UserDto getUserById(int userId);

    UserDto getUserByEmail(String email);

    UserDto updateProfile(int userId, UserDto updatedUser);

    void changePassword(int userId, String newPassword);

    void deactivateAccount(int userId);

    List<UserDto> getAllUsers();
}