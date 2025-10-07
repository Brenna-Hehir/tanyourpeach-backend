package com.tanyourpeach.backend.controller;

import com.tanyourpeach.backend.dto.UserCreateDto;
import com.tanyourpeach.backend.dto.UserUpdateDto;
import com.tanyourpeach.backend.model.User;
import com.tanyourpeach.backend.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

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

        ResponseEntity<?> response = controller.getAllUsers();
        assertEquals(HttpStatus.OK, response.getStatusCode());

        @SuppressWarnings("unchecked")
        List<User> result = (List<User>) response.getBody();

        assertEquals(1, result.size());
        assertEquals("Brenna", result.get(0).getName());
    }

    @Test
    void getUserById_shouldReturnUser_ifFound() {
        when(userService.getUserByIdOrThrow(1L)).thenReturn(testUser);

        ResponseEntity<User> response = controller.getUserById(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(testUser, response.getBody());
    }

    @Test
    void getUserById_shouldThrow404_ifNotFound() {
        when(userService.getUserByIdOrThrow(1L))
            .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
            () -> controller.getUserById(1L));
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    @Test
    void createUser_shouldReturnCreatedUser() {
        UserCreateDto dto = new UserCreateDto();
        dto.setName("Brenna");
        dto.setEmail("brenna@example.com");
        dto.setPassword("secret");
        dto.setAddress("Peach St");
        dto.setIsAdmin(false);

        when(userService.createUser(any(UserCreateDto.class))).thenReturn(testUser);

        ResponseEntity<User> response = controller.createUser(dto);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(testUser, response.getBody());
    }

    @Test
    void createUser_shouldPropagate400_whenServiceSignalsBadRequest() {
        UserCreateDto dto = new UserCreateDto();
        // intentionally leave invalid to simulate service signaling a 400 (in real life, @Valid would catch this at MVC layer)
        when(userService.createUser(any(UserCreateDto.class)))
            .thenThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST, "bad"));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
            () -> controller.createUser(dto));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
    }

    @Test
    void updateUser_shouldReturnUpdated_ifFound() {
        UserUpdateDto dto = new UserUpdateDto();
        dto.setName("Updated");
        dto.setEmail("updated@example.com");
        dto.setPassword("newSecret");
        dto.setAddress("New Address");
        dto.setIsAdmin(true);

        when(userService.updateUser(eq(1L), any(UserUpdateDto.class))).thenReturn(testUser);

        ResponseEntity<User> response = controller.updateUser(1L, dto);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(testUser, response.getBody());
    }

    @Test
    void updateUser_shouldThrow404_ifNotFound() {
        UserUpdateDto dto = new UserUpdateDto();
        dto.setName("Updated");
        dto.setEmail("updated@example.com");
        dto.setPassword("pw");

        when(userService.updateUser(eq(1L), any(UserUpdateDto.class)))
            .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
            () -> controller.updateUser(1L, dto));
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    @Test
    void deleteUser_shouldReturn204_ifSuccessful() {
        // service is void; no exception means success
        doNothing().when(userService).deleteUser(1L);

        ResponseEntity<Void> response = controller.deleteUser(1L);
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(userService).deleteUser(1L);
    }

    @Test
    void deleteUser_shouldThrow404_whenUserDoesNotExist() {
        doThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"))
            .when(userService).deleteUser(999L);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
            () -> controller.deleteUser(999L));

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
        verify(userService).deleteUser(999L);
    }
}