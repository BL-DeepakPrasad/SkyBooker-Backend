package com.skybooker.auth.service;

import com.skybooker.auth.dto.*;
import com.skybooker.auth.exception.EmailAlreadyExistsException;
import com.skybooker.auth.exception.InvalidCredentialsException;
import com.skybooker.auth.exception.UserNotFoundException;
import com.skybooker.auth.model.User;
import com.skybooker.auth.repository.UserRepository;
import com.skybooker.auth.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private static final Logger LOGGER = LogManager.getLogger(AuthServiceImpl.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Override
    public UserDto register(RegisterRequest request) {

        LOGGER.info("Registering user with email: {}", request.getEmail());

        if (userRepository.existsByEmail(request.getEmail())) {
            LOGGER.warn("Registration failed. Email already exists: {}", request.getEmail());
            throw new EmailAlreadyExistsException("Email already registered: " + request.getEmail());
        }

        User user = new User();
        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setPhone(request.getPhone());
        user.setRole(request.getRole() != null ? request.getRole() : "ROLE_USER");
        user.setNationality(request.getNationality());
        user.setPassportNumber(request.getPassportNumber());
        user.setActive(true);

        User savedUser = userRepository.save(user);

        LOGGER.info("User registered successfully. User Id: {}", savedUser.getUserId());

        return mapToDto(savedUser);
    }

    @Override
    public AuthResponse login(LoginRequest request) {

        LOGGER.info("Login attempt for email: {}", request.getEmail());

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> {
                    LOGGER.error("User not found: {}", request.getEmail());
                    return new UserNotFoundException("User not found with email: " + request.getEmail());
                });

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            LOGGER.error("Invalid credentials for email: {}", request.getEmail());
            throw new InvalidCredentialsException("Invalid credentials for email: " + request.getEmail());
        }

        String token = jwtService.generateToken(user);

        LOGGER.info("Login successful for user: {}", request.getEmail());

        return new AuthResponse(token, mapToDto(user));
    }

    @Override
    public void logout(String token) {
        LOGGER.info("Logout requested");
        // Optional: Add token to blacklist table/cache
        LOGGER.info("Logout successful");
    }

    @Override
    public boolean validateToken(String token) {
        LOGGER.debug("Validating token");
        return jwtService.validateToken(token);
    }

    @Override
    public AuthResponse refreshToken(String token) {
        LOGGER.info("Refreshing token");

        if (!jwtService.validateToken(token)) {
            LOGGER.error("Invalid token provided for refresh");
            throw new RuntimeException("Invalid token");
        }

        String email = jwtService.extractUsername(token);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found with email: " + email));

        String newToken = jwtService.generateToken(user);

        return new AuthResponse(newToken, mapToDto(user));
    }

    @Override
    public UserDto getUserById(int userId) {
        LOGGER.info("Fetching user by id: {}", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    LOGGER.error("User not found with id: {}", userId);
                    return new UserNotFoundException("User not found with id: " + userId);
                });
        return mapToDto(user);
    }

    @Override
    public UserDto getUserByEmail(String email) {
        LOGGER.info("Fetching user by email: {}", email);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    LOGGER.error("User not found with email: {}", email);
                    return new UserNotFoundException("User not found with email: " + email);
                });
        return mapToDto(user);
    }

    @Override
    public UserDto updateProfile(int userId, UserDto updatedUser) {
        LOGGER.info("Updating profile for user id: {}", userId);

        User existingUser = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));

        existingUser.setFullName(updatedUser.getFullName());
        existingUser.setPhone(updatedUser.getPhone());
        existingUser.setNationality(updatedUser.getNationality());
        existingUser.setPassportNumber(updatedUser.getPassportNumber());

        User savedUser = userRepository.save(existingUser);

        LOGGER.info("Profile updated successfully for user id: {}", userId);

        return mapToDto(savedUser);
    }

    @Override
    public void changePassword(int userId, String newPassword) {
        LOGGER.info("Changing password for user id: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));

        user.setPasswordHash(passwordEncoder.encode(newPassword));

        userRepository.save(user);

        LOGGER.info("Password changed successfully for user id: {}", userId);
    }

    @Override
    public void deactivateAccount(int userId) {
        LOGGER.info("Deactivating account for user id: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));

        user.setActive(false);

        userRepository.save(user);

        LOGGER.info("Account deactivated successfully for user id: {}", userId);
    }

    @Override
    public List<UserDto> getAllUsers() {
        LOGGER.info("Fetching all users");

        return userRepository.findAll().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    private UserDto mapToDto(User user) {
        return UserDto.builder()
                .userId(user.getUserId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .role(user.getRole())
                .provider(user.getProvider())
                .isActive(user.isActive())
                .passportNumber(user.getPassportNumber())
                .nationality(user.getNationality())
                .createdAt(user.getCreatedAt())
                .build();
    }
}