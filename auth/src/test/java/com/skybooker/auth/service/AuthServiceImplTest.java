package com.skybooker.auth.service;

import com.skybooker.auth.dto.*;
import com.skybooker.auth.exception.EmailAlreadyExistsException;
import com.skybooker.auth.exception.InvalidCredentialsException;
import com.skybooker.auth.exception.UserNotFoundException;
import com.skybooker.auth.model.User;
import com.skybooker.auth.repository.UserRepository;
import com.skybooker.auth.security.JwtService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthServiceImpl authService;

    @Test
    void shouldRegisterUserSuccessfully() {

        RegisterRequest request = new RegisterRequest();
        request.setEmail("test@test.com");
        request.setPassword("password");
        request.setFullName("Test User");

        User savedUser = new User();
        savedUser.setUserId(1);
        savedUser.setEmail("test@test.com");
        savedUser.setFullName("Test User");

        when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(passwordEncoder.encode("password")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        UserDto result = authService.register(request);

        assertNotNull(result);
        assertEquals(1, result.getUserId());
        assertEquals("test@test.com", result.getEmail());

        verify(userRepository).save(any(User.class));
    }

    @Test
    void shouldThrowExceptionWhenEmailAlreadyExists() {

        RegisterRequest request = new RegisterRequest();
        request.setEmail("test@test.com");

        when(userRepository.existsByEmail(request.getEmail())).thenReturn(true);

        assertThrows(EmailAlreadyExistsException.class, () -> authService.register(request));
    }

    @Test
    void shouldLoginSuccessfully() {

        LoginRequest request = new LoginRequest("test@test.com", "password");

        User user = new User();
        user.setEmail("test@test.com");
        user.setPasswordHash("encodedPassword");

        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password", "encodedPassword")).thenReturn(true);
        when(jwtService.generateToken(user)).thenReturn("jwt-token");

        AuthResponse response = authService.login(request);

        assertEquals("jwt-token", response.getToken());
    }

    @Test
    void shouldFailLoginForInvalidPassword() {

        LoginRequest request = new LoginRequest("test@test.com", "wrongPassword");

        User user = new User();
        user.setEmail("test@test.com");
        user.setPasswordHash("encodedPassword");

        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrongPassword", "encodedPassword")).thenReturn(false);

        assertThrows(InvalidCredentialsException.class, () -> authService.login(request));
    }

    @Test
    void shouldGetUserById() {

        User user = new User();
        user.setUserId(1);
        user.setEmail("test@test.com");

        when(userRepository.findById(1)).thenReturn(Optional.of(user));

        UserDto result = authService.getUserById(1);

        assertEquals(1, result.getUserId());
    }

    @Test
    void shouldUpdateProfile() {

        User existingUser = new User();
        existingUser.setUserId(1);
        existingUser.setFullName("Old Name");

        UserDto updatedUser = new UserDto();
        updatedUser.setFullName("Deepak");

        when(userRepository.findById(1)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(User.class))).thenReturn(existingUser);

        UserDto result = authService.updateProfile(1, updatedUser);

        assertNotNull(result);
        verify(userRepository).save(existingUser);
    }

    @Test
    void shouldChangePassword() {

        User user = new User();
        user.setUserId(1);

        when(userRepository.findById(1)).thenReturn(Optional.of(user));
        when(passwordEncoder.encode("newPassword")).thenReturn("encodedPassword");

        authService.changePassword(1, "newPassword");

        verify(userRepository).save(user);
    }

    @Test
    void shouldDeactivateAccount() {

        User user = new User();
        user.setUserId(1);
        user.setActive(true);

        when(userRepository.findById(1)).thenReturn(Optional.of(user));

        authService.deactivateAccount(1);

        verify(userRepository).save(user);
        assertFalse(user.isActive());
    }

    @Test
    void shouldValidateToken() {

        when(jwtService.validateToken("token")).thenReturn(true);

        boolean result = authService.validateToken("token");

        assertTrue(result);
    }

    @Test
    void shouldGetAllUsers() {

        List<User> users = List.of(new User(), new User());

        when(userRepository.findAll()).thenReturn(users);

        List<UserDto> result = authService.getAllUsers();

        assertEquals(2, result.size());
    }
}
