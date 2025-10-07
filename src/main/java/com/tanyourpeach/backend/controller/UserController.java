package com.tanyourpeach.backend.controller;

import com.tanyourpeach.backend.dto.UserCreateDto;
import com.tanyourpeach.backend.dto.UserUpdateDto;
import com.tanyourpeach.backend.model.User;
import com.tanyourpeach.backend.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
@Validated
public class UserController {

    @Autowired
    private UserService userService;

    // Admin-only listing (leave this as protected)
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    // Public: get user by id (404 if missing -> unified JSON via handler)
    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserByIdOrThrow(id));
    }

    // Public: create (400 on validation, 409 on duplicate -> unified JSON)
    @PostMapping
    public ResponseEntity<User> createUser(@Valid @RequestBody UserCreateDto dto) {
        User created = userService.createUser(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    // Public: update (404 if missing, 409 email collision)
    @PutMapping("/{id}")
    public ResponseEntity<User> updateUser(@PathVariable Long id, @Valid @RequestBody UserUpdateDto dto) {
        return ResponseEntity.ok(userService.updateUser(id, dto));
    }

    // Public: delete (404 if missing)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}
