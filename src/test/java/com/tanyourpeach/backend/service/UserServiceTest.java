package com.tanyourpeach.backend.service;

import com.tanyourpeach.backend.model.User;
import com.tanyourpeach.backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private User testUser;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        testUser = new User();
        testUser.setUserId(1L);
        testUser.setName("Brenna");
        testUser.setEmail("brenna@example.com");
        testUser.setPasswordHash("hashed_pw");
        testUser.setAddress("Peach St, GA");
        testUser.setIsAdmin(false);
    }

    @Test
    void getAllUsers_shouldReturnList() {
        when(userRepository.findAll()).thenReturn(List.of(testUser));
        List<User> users = userService.getAllUsers();
        assertEquals(1, users.size());
    }

    @Test
    void getUserById_shouldReturnUser_whenExists() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        Optional<User> result = userService.getUserById(1L);
        assertTrue(result.isPresent());
        assertEquals("Brenna", result.get().getName());
    }

    @Test
    void getUserById_shouldReturnEmpty_whenNotFound() {
        when(userRepository.findById(2L)).thenReturn(Optional.empty());
        Optional<User> result = userService.getUserById(2L);
        assertTrue(result.isEmpty());
    }

    @Test
    void createUser_shouldSaveAndReturnUser() {
        when(userRepository.save(testUser)).thenReturn(testUser);
        User result = userService.createUser(testUser);
        assertNotNull(result);
        assertEquals("brenna@example.com", result.getEmail());
    }

    @Test
    void createUser_shouldFail_whenNameMissing() {
        User invalid = new User();
        invalid.setName(" ");
        invalid.setEmail("valid@example.com");
        invalid.setPasswordHash("valid_pw");

        RuntimeException ex = assertThrows(RuntimeException.class, () -> userService.createUser(invalid));
        assertEquals("Name is required", ex.getMessage());
        verify(userRepository, never()).save(any());
    }

    @Test
    void createUser_shouldFail_whenEmailMissing() {
        User invalid = new User();
        invalid.setName("Valid");
        invalid.setEmail("  ");
        invalid.setPasswordHash("valid_pw");

        RuntimeException ex = assertThrows(RuntimeException.class, () -> userService.createUser(invalid));
        assertEquals("Email is required", ex.getMessage());
        verify(userRepository, never()).save(any());
    }

    @Test
    void createUser_shouldFail_whenPasswordMissing() {
        User invalid = new User();
        invalid.setName("Valid");
        invalid.setEmail("valid@example.com");
        invalid.setPasswordHash("  "); // blank password

        RuntimeException ex = assertThrows(RuntimeException.class, () -> userService.createUser(invalid));
        assertEquals("Password is required for registered users", ex.getMessage());
        verify(userRepository, never()).save(any());
    }

    @Test
    void updateUser_shouldUpdate_whenExists() {
        User updated = new User();
        updated.setName("Updated");
        updated.setEmail("updated@example.com");
        updated.setPasswordHash("new_hash");
        updated.setAddress("New Address");
        updated.setIsAdmin(true);

        when(userRepository.existsById(1L)).thenReturn(true);
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        Optional<User> result = userService.updateUser(1L, updated);

        assertTrue(result.isPresent());
        assertEquals("updated@example.com", result.get().getEmail());
        assertTrue(result.get().getIsAdmin()); // ✅ Now should pass
    }

    @Test
    void updateUser_shouldReturnEmpty_whenNotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());
        Optional<User> result = userService.updateUser(99L, testUser);
        assertTrue(result.isEmpty());
    }

    @Test
    void updateUser_shouldFail_whenNameMissing() {
        when(userRepository.existsById(1L)).thenReturn(true);
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        User invalid = new User();
        invalid.setName(" ");
        invalid.setEmail("valid@example.com");
        invalid.setPasswordHash("valid_pw");

        RuntimeException ex = assertThrows(RuntimeException.class, () -> userService.updateUser(1L, invalid));
        assertEquals("Name is required", ex.getMessage());
        verify(userRepository, never()).save(any());
    }

    @Test
    void updateUser_shouldFail_whenEmailMissing() {
        when(userRepository.existsById(1L)).thenReturn(true);
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        User invalid = new User();
        invalid.setName("Valid");
        invalid.setEmail(" ");
        invalid.setPasswordHash("valid_pw");

        RuntimeException ex = assertThrows(RuntimeException.class, () -> userService.updateUser(1L, invalid));
        assertEquals("Email is required", ex.getMessage());
        verify(userRepository, never()).save(any());
    }

    @Test
    void updateUser_shouldFail_whenPasswordMissing() {
        when(userRepository.existsById(1L)).thenReturn(true);
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        User invalid = new User();
        invalid.setName("Valid");
        invalid.setEmail("valid@example.com");
        invalid.setPasswordHash("  "); // blank password
        invalid.setAddress("Some address");

        RuntimeException ex = assertThrows(RuntimeException.class, () -> userService.updateUser(1L, invalid));
        assertEquals("Password is required for registered users", ex.getMessage());
        verify(userRepository, never()).save(any());
    }

    @Test
    void deleteUser_shouldReturnTrue_whenExists() {
        when(userRepository.existsById(1L)).thenReturn(true);
        boolean deleted = userService.deleteUser(1L);
        assertTrue(deleted);
        verify(userRepository).deleteById(1L);
    }

    @Test
    void deleteUser_shouldReturnFalse_whenNotFound() {
        when(userRepository.existsById(999L)).thenReturn(false);
        boolean deleted = userService.deleteUser(999L);
        assertFalse(deleted);
        verify(userRepository, never()).deleteById(anyLong());
    }
}