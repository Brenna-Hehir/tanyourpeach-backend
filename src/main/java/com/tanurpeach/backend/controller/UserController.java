package com.tanurpeach.backend.controller;

import com.tanurpeach.backend.model.User;
import com.tanurpeach.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*") // allow frontend to access
public class UserController {

    @Autowired
    private UserRepository userRepository;

    // GET all users
    @GetMapping
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    // GET user by ID
    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        Optional<User> user = userRepository.findById(id);
        return user.map(ResponseEntity::ok)
                   .orElseGet(() -> ResponseEntity.notFound().build());
    }
    
    // POST create a new user
    @PostMapping
    public User createUser(@RequestBody User user) {
        return userRepository.save(user);
    }

    // PUT update an existing user
    @PutMapping("/{id}")
    public ResponseEntity<User> updateUser(@PathVariable Long id, @RequestBody User updatedUser) {
        return userRepository.findById(id)
            .map(user -> {
                user.setName(updatedUser.getName());
                user.setEmail(updatedUser.getEmail());
                user.setPasswordHash(updatedUser.getPasswordHash());
                user.setAddress(updatedUser.getAddress());
                user.setIsAdmin(updatedUser.getIsAdmin());
                return ResponseEntity.ok(userRepository.save(user));
            })
            .orElse(ResponseEntity.notFound().build());
    }

    // DELETE user by ID
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        if (!userRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        userRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
