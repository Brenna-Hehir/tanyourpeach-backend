package com.tanyourpeach.backend.service;

import com.tanyourpeach.backend.model.User;
import com.tanyourpeach.backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CustomUserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CustomUserDetailsService customUserDetailsService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void loadUserByUsername_shouldReturnUserDetails_whenUserExists() {
        User mockUser = new User();
        mockUser.setEmail("admin@example.com");
        mockUser.setPasswordHash("hashedPassword123");
        mockUser.setIsAdmin(true);

        when(userRepository.findByEmail("admin@example.com")).thenReturn(Optional.of(mockUser));

        UserDetails userDetails = customUserDetailsService.loadUserByUsername("admin@example.com");

        assertEquals("admin@example.com", userDetails.getUsername());
        assertEquals("hashedPassword123", userDetails.getPassword());
        assertTrue(userDetails.getAuthorities().stream().anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN")));
    }

    @Test
    void loadUserByUsername_shouldAssignUserRole_whenNotAdmin() {
        User user = new User();
        user.setEmail("user@example.com");
        user.setPasswordHash("pass123");
        user.setIsAdmin(false);

        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));

        UserDetails userDetails = customUserDetailsService.loadUserByUsername("user@example.com");

        assertTrue(userDetails.getAuthorities().stream().anyMatch(auth -> auth.getAuthority().equals("ROLE_USER")));
    }

    @Test
    void loadUserByUsername_shouldAssignSingleAuthority() {
        User user = new User();
        user.setEmail("singleauth@example.com");
        user.setPasswordHash("pass456");
        user.setIsAdmin(false);

        when(userRepository.findByEmail("singleauth@example.com")).thenReturn(Optional.of(user));

        UserDetails userDetails = customUserDetailsService.loadUserByUsername("singleauth@example.com");

        assertEquals(1, userDetails.getAuthorities().size());
        assertTrue(userDetails.getAuthorities().stream().anyMatch(auth -> auth.getAuthority().equals("ROLE_USER")));
    }

    @Test
    void loadUserByUsername_shouldReturnUserDetails_evenIfPasswordEmpty() {
        User user = new User();
        user.setEmail("empty@example.com");
        user.setPasswordHash(""); // Empty password
        user.setIsAdmin(false);

        when(userRepository.findByEmail("empty@example.com")).thenReturn(Optional.of(user));

        UserDetails userDetails = customUserDetailsService.loadUserByUsername("empty@example.com");

        assertEquals("empty@example.com", userDetails.getUsername());
        assertEquals("", userDetails.getPassword());
    }

    @Test
    void loadUserByUsername_shouldThrowException_whenUserNotFound() {
        when(userRepository.findByEmail("missing@example.com")).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () -> {
            customUserDetailsService.loadUserByUsername("missing@example.com");
        });
    }
}