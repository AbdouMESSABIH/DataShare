package com.datashare.backend.service;

import com.datashare.backend.dto.LoginRequest;
import com.datashare.backend.dto.RegisterRequest;
import com.datashare.backend.entity.User;
import com.datashare.backend.repository.UserRepository;
import com.datashare.backend.security.JwtService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AuthServiceTest {

    private UserRepository userRepository;
    private PasswordEncoder passwordEncoder;
    private JwtService jwtService;
    private AuthService authService;


    @BeforeEach
    void setUp() {

        userRepository =
                mock(UserRepository.class);

        passwordEncoder =
                mock(PasswordEncoder.class);

        jwtService =
                mock(JwtService.class);

        authService =
                new AuthService(
                        userRepository,
                        passwordEncoder,
                        jwtService
                );
    }


    @Test
    void registerShouldRejectExistingEmail() {

        RegisterRequest request =
                mock(RegisterRequest.class);

        when(request.getEmail())
                .thenReturn("test@test.com");

        when(userRepository.existsByEmail("test@test.com"))
                .thenReturn(true);

        ResponseStatusException exception =
                assertThrows(
                        ResponseStatusException.class,
                        () -> authService.register(request)
                );

        assertEquals(
                HttpStatus.CONFLICT,
                exception.getStatusCode()
        );
    }


    @Test
    void registerShouldEncodePasswordBeforeSavingUser() {

        RegisterRequest request =
                mock(RegisterRequest.class);

        when(request.getEmail())
                .thenReturn(" Test@Test.com ");

        when(request.getPassword())
                .thenReturn("Password123");

        when(userRepository.existsByEmail("test@test.com"))
                .thenReturn(false);

        when(passwordEncoder.encode("Password123"))
                .thenReturn("HASHED_PASSWORD");

        authService.register(request);

        ArgumentCaptor<User> userCaptor =
                ArgumentCaptor.forClass(User.class);

        verify(userRepository)
                .save(userCaptor.capture());

        User savedUser =
                userCaptor.getValue();

        assertEquals(
                "test@test.com",
                savedUser.getEmail()
        );

        assertEquals(
                "HASHED_PASSWORD",
                savedUser.getPasswordHash()
        );

        assertNotEquals(
                "Password123",
                savedUser.getPasswordHash()
        );
    }


    @Test
    void loginShouldRejectUnknownEmail() {

        LoginRequest request =
                mock(LoginRequest.class);

        when(request.getEmail())
                .thenReturn("unknown@test.com");

        when(userRepository.findByEmail("unknown@test.com"))
                .thenReturn(Optional.empty());

        ResponseStatusException exception =
                assertThrows(
                        ResponseStatusException.class,
                        () -> authService.login(request)
                );

        assertEquals(
                HttpStatus.UNAUTHORIZED,
                exception.getStatusCode()
        );
    }


    @Test
    void loginShouldRejectInvalidPassword() {

        LoginRequest request =
                mock(LoginRequest.class);

        User user =
                mock(User.class);

        when(request.getEmail())
                .thenReturn("test@test.com");

        when(request.getPassword())
                .thenReturn("WrongPassword");

        when(userRepository.findByEmail("test@test.com"))
                .thenReturn(Optional.of(user));

        when(user.getPasswordHash())
                .thenReturn("HASHED_PASSWORD");

        when(passwordEncoder.matches(
                "WrongPassword",
                "HASHED_PASSWORD"
        ))
                .thenReturn(false);

        ResponseStatusException exception =
                assertThrows(
                        ResponseStatusException.class,
                        () -> authService.login(request)
                );

        assertEquals(
                HttpStatus.UNAUTHORIZED,
                exception.getStatusCode()
        );
    }


    @Test
    void loginShouldReturnJwtWhenCredentialsAreValid() {

        LoginRequest request =
                mock(LoginRequest.class);

        User user =
                mock(User.class);

        when(request.getEmail())
                .thenReturn(" Test@Test.com ");

        when(request.getPassword())
                .thenReturn("Password123");

        when(userRepository.findByEmail("test@test.com"))
                .thenReturn(Optional.of(user));

        when(user.getPasswordHash())
                .thenReturn("HASHED_PASSWORD");

        when(user.getEmail())
                .thenReturn("test@test.com");

        when(passwordEncoder.matches(
                "Password123",
                "HASHED_PASSWORD"
        ))
                .thenReturn(true);

        when(jwtService.generateToken("test@test.com"))
                .thenReturn("fake-jwt-token");

        String token =
                authService.login(request);

        assertEquals(
                "fake-jwt-token",
                token
        );

        verify(jwtService)
                .generateToken("test@test.com");
    }
}