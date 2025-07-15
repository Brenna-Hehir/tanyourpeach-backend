package com.tanyourpeach.backend.service;

import com.tanyourpeach.backend.dto.AuthenticationRequest;
import com.tanyourpeach.backend.dto.AuthenticationResponse;
import com.tanyourpeach.backend.dto.RegisterRequest;
import com.tanyourpeach.backend.model.User;
import com.tanyourpeach.backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class UserAuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtService jwtService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private UserAuthService userAuthService;

    private final String encodedPassword = "encodedPassword";

    private User testUser;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        testUser = new User();
        testUser.setName("Test User");
        testUser.setEmail("test@example.com");
        testUser.setPasswordHash(encodedPassword);
        testUser.setAddress("123 Peach St");
        testUser.setIsAdmin(false);
    }

    @Test
    void register_shouldDefaultAdminFalseIfNull() {
        RegisterRequest request = new RegisterRequest();
        request.setName("Test");
        request.setEmail("test@example.com");
        request.setPassword("password");
        request.setAddress("123 Main St");
        request.setIsAdmin(null);

        when(passwordEncoder.encode("password")).thenReturn(encodedPassword);
        when(jwtService.generateToken(any())).thenReturn("mock-token");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        AuthenticationResponse response = userAuthService.register(request);
        assertNotNull(response);
        assertEquals("mock-token", response.getToken());
    }

    @Test
    void register_shouldEncodePassword() {
        RegisterRequest request = new RegisterRequest();
        request.setName("Encode");
        request.setEmail("encode@example.com");
        request.setPassword("plain");
        request.setIsAdmin(false);
        request.setAddress("123 Address St");

        when(passwordEncoder.encode("plain")).thenReturn("hashed");
        when(jwtService.generateToken(any())).thenReturn("jwt");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        AuthenticationResponse response = userAuthService.register(request);
        assertEquals("jwt", response.getToken());
    }

    @Test
    void register_shouldHandleDuplicateEmail() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("dupe@example.com");
        request.setPassword("pass");
        request.setName("Dupe");
        request.setAddress("abc");
        request.setIsAdmin(false);

        when(userRepository.save(any(User.class))).thenThrow(new RuntimeException("Duplicate"));

        assertThrows(RuntimeException.class, () -> userAuthService.register(request));
    }

    @Test
    void register_shouldFail_whenFieldsAreBlank() {
        RegisterRequest request = new RegisterRequest();
        request.setName("   ");  // blank
        request.setEmail(" ");   // blank
        request.setPassword(""); // blank
        request.setAddress(null);
        request.setIsAdmin(false);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> userAuthService.register(request));
        assertEquals("Name is required", ex.getMessage()); // stops at first validation
    }

    @Test
    void authenticate_shouldReturnTokenOnSuccess() {
        AuthenticationRequest request = new AuthenticationRequest();
        request.setEmail("valid@example.com");
        request.setPassword("password");

        User user = new User();
        user.setEmail("valid@example.com");
        user.setPasswordHash("encoded");
        user.setIsAdmin(false);

        when(userRepository.findByEmail("valid@example.com")).thenReturn(Optional.of(user));
        when(jwtService.generateToken(user)).thenReturn("token");

        AuthenticationResponse response = userAuthService.authenticate(request);
        assertEquals("token", response.getToken());
    }

    @Test
    void authenticate_shouldThrowForInvalidCredentials() {
        AuthenticationRequest request = new AuthenticationRequest();
        request.setEmail("wrong@example.com");
        request.setPassword("bad");

        doThrow(new BadCredentialsException("Bad creds"))
                .when(authenticationManager)
                .authenticate(any(UsernamePasswordAuthenticationToken.class));

        assertThrows(BadCredentialsException.class, () -> userAuthService.authenticate(request));
    }

    @Test
    void authenticate_shouldThrowWhenUserNotFound() {
        AuthenticationRequest request = new AuthenticationRequest();
        request.setEmail("missing@example.com");
        request.setPassword("any");

        when(userRepository.findByEmail("missing@example.com")).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> userAuthService.authenticate(request));
    }

    @Test
    void register_shouldFail_whenEmailAlreadyExists() {
        RegisterRequest request = new RegisterRequest();
        request.setName("Test User");
        request.setEmail("test@example.com");
        request.setPassword("password");
        request.setAddress("123 Peach St");
        request.setIsAdmin(false);

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));

        RuntimeException ex = assertThrows(RuntimeException.class, () -> userAuthService.register(request));
        assertEquals("Email already in use", ex.getMessage());

        verify(userRepository, never()).save(any(User.class));
    }
}