package com.tanyourpeach.backend.controller;

import com.tanyourpeach.backend.model.User;
import com.tanyourpeach.backend.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class UserControllerTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController controller;

    private User testUser;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        testUser = new User();
        testUser.setUserId(1L);
        testUser.setName("Brenna");
        testUser.setEmail("brenna@example.com");
        testUser.setPasswordHash("hashedPassword");
    }

    @Test
    void getAllUsers_shouldReturnList() {
        when(userService.getAllUsers()).thenReturn(List.of(testUser));

        List<User> result = controller.getAllUsers();

        assertEquals(1, result.size());
        assertEquals("Brenna", result.get(0).getName());
    }

    @Test
    void getUserById_shouldReturnUser_ifFound() {
        when(userService.getUserById(1L)).thenReturn(Optional.of(testUser));

        ResponseEntity<User> response = controller.getUserById(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(testUser, response.getBody());
    }

    @Test
    void getUserById_shouldReturn404_ifNotFound() {
        when(userService.getUserById(1L)).thenReturn(Optional.empty());

        ResponseEntity<User> response = controller.getUserById(1L);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void createUser_shouldReturnCreatedUser() {
        when(userService.createUser(testUser)).thenReturn(testUser);

        User result = controller.createUser(testUser);

        assertNotNull(result);
        assertEquals("Brenna", result.getName());
    }

    @Test
    void createUser_shouldReturnNull_whenInvalidInput() {
        User invalidUser = new User();
        invalidUser.setName(" "); // blank name
        invalidUser.setEmail(" "); // blank email

        when(userService.createUser(any())).thenReturn(null);

        User result = controller.createUser(invalidUser);
        assertNull(result);
        verify(userService).createUser(any());
    }

    @Test
    void createUser_shouldReturnNull_whenFieldsAreNull() {
        User nullUser = new User();
        nullUser.setName(null);
        nullUser.setEmail(null);

        when(userService.createUser(any())).thenReturn(null);

        User result = controller.createUser(nullUser);
        assertNull(result);
        verify(userService).createUser(any());
    }

    @Test
    void updateUser_shouldReturnUpdated_ifFound() {
        when(userService.updateUser(eq(1L), any())).thenReturn(Optional.of(testUser));

        ResponseEntity<User> response = controller.updateUser(1L, testUser);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(testUser, response.getBody());
    }

    @Test
    void updateUser_shouldReturnEmpty_whenInvalidInput() {
        User invalidUpdate = new User();
        invalidUpdate.setName(""); // blank name
        invalidUpdate.setEmail("bademail"); // assume invalid format

        when(userService.updateUser(eq(1L), any())).thenReturn(Optional.empty());

        ResponseEntity<User> response = controller.updateUser(1L, invalidUpdate);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(userService).updateUser(eq(1L), any());
    }

    @Test
    void updateUser_shouldReturnNotFound_whenUserDoesNotExist() {
        User updatedUser = new User();
        updatedUser.setName("Updated");
        updatedUser.setEmail("updated@example.com");

        when(userService.updateUser(eq(99L), any())).thenReturn(Optional.empty());

        ResponseEntity<User> response = controller.updateUser(99L, updatedUser);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(userService).updateUser(eq(99L), any());
    }

    @Test
    void updateUser_shouldReturn404_ifNotFound() {
        when(userService.updateUser(eq(1L), any())).thenReturn(Optional.empty());

        ResponseEntity<User> response = controller.updateUser(1L, testUser);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void deleteUser_shouldReturn204_ifSuccessful() {
        when(userService.deleteUser(1L)).thenReturn(true);

        ResponseEntity<Void> response = controller.deleteUser(1L);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
    }

    @Test
    void deleteUser_shouldReturnNotFound_whenUserDoesNotExist() {
        when(userService.deleteUser(999L)).thenReturn(false);

        ResponseEntity<Void> response = controller.deleteUser(999L);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(userService).deleteUser(999L);
    }

    @Test
    void deleteUser_shouldReturn404_ifNotFound() {
        when(userService.deleteUser(1L)).thenReturn(false);

        ResponseEntity<Void> response = controller.deleteUser(1L);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }
}