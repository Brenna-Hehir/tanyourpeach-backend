package com.tanyourpeach.backend.service;

import com.tanyourpeach.backend.dto.UserCreateDto;
import com.tanyourpeach.backend.dto.UserUpdateDto;
import com.tanyourpeach.backend.model.User;
import com.tanyourpeach.backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
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
        User result = userService.getUserByIdOrThrow(1L);
        assertNotNull(result);
        assertEquals("Brenna", result.getName());
    }

    @Test
    void getUserById_shouldThrow404_whenNotFound() {
        when(userRepository.findById(2L)).thenReturn(Optional.empty());
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> userService.getUserByIdOrThrow(2L));
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    @Test
    void createUser_shouldSaveAndReturnUser() {
        UserCreateDto dto = new UserCreateDto();
        dto.setName("Brenna");
        dto.setEmail("brenna@example.com");
        dto.setPassword("pw");
        dto.setAddress("Addr");
        dto.setIsAdmin(false);

        when(userRepository.findByEmail("brenna@example.com")).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        User result = userService.createUser(dto);
        assertNotNull(result);
        assertEquals("brenna@example.com", result.getEmail());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void createUser_shouldThrow409_whenEmailDuplicate() {
        UserCreateDto dto = new UserCreateDto();
        dto.setName("Brenna");
        dto.setEmail("dup@example.com");
        dto.setPassword("pw");

        when(userRepository.findByEmail("dup@example.com")).thenReturn(Optional.of(testUser));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> userService.createUser(dto));
        assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());
        verify(userRepository, never()).save(any());
    }

    @Test
    void updateUser_shouldUpdate_whenExists() {
        UserUpdateDto dto = new UserUpdateDto();
        dto.setName("Updated");
        dto.setEmail("updated@example.com");
        dto.setPassword("new_hash");
        dto.setAddress("New Address");
        dto.setIsAdmin(true);

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.findByEmail("updated@example.com")).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        User result = userService.updateUser(1L, dto);

        assertNotNull(result);
        assertEquals("updated@example.com", result.getEmail());
        assertTrue(result.getIsAdmin());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void updateUser_shouldThrow404_whenNotFound() {
        UserUpdateDto dto = new UserUpdateDto();
        dto.setName("Updated");
        dto.setEmail("updated@example.com");
        dto.setPassword("pw");

        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> userService.updateUser(99L, dto));
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    @Test
    void updateUser_shouldThrow409_whenEmailCollision() {
        UserUpdateDto dto = new UserUpdateDto();
        dto.setName("Updated");
        dto.setEmail("dup@example.com");
        dto.setPassword("pw");

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.findByEmail("dup@example.com")).thenReturn(Optional.of(new User()));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> userService.updateUser(1L, dto));
        assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());
        verify(userRepository, never()).save(any());
    }

    @Test
    void deleteUser_shouldSucceed_whenExists() {
        when(userRepository.existsById(1L)).thenReturn(true);
        doNothing().when(userRepository).deleteById(1L);

        assertDoesNotThrow(() -> userService.deleteUser(1L));
        verify(userRepository).deleteById(1L);
    }

    @Test
    void deleteUser_shouldThrow404_whenMissing() {
        when(userRepository.existsById(999L)).thenReturn(false);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> userService.deleteUser(999L));
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
        verify(userRepository, never()).deleteById(anyLong());
    }
}