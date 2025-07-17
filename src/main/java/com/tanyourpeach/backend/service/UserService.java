package com.tanyourpeach.backend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.tanyourpeach.backend.model.User;
import com.tanyourpeach.backend.repository.UserRepository;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    // GET all users
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    // GET user by ID
    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    // POST create new user
    public User createUser(User user) {
        validateUserFields(user);
        return userRepository.save(user);
    }

    // PUT update user
    public Optional<User> updateUser(Long id, User updatedUser) {
        if (!userRepository.existsById(id)) return Optional.empty();
        validateUserFields(updatedUser);

        return userRepository.findById(id).map(user -> {
            user.setName(updatedUser.getName());
            user.setEmail(updatedUser.getEmail());
            user.setPasswordHash(updatedUser.getPasswordHash());
            user.setAddress(updatedUser.getAddress());
            user.setIsAdmin(updatedUser.getIsAdmin());
            return userRepository.save(user);
        });
    }

    // DELETE user
    public boolean deleteUser(Long id) {
        if (!userRepository.existsById(id)) return false;
        userRepository.deleteById(id);
        return true;
    }

    // Validate required fields
    private void validateUserFields(User user) {
        if (user.getName() == null || user.getName().trim().isEmpty()) {
            throw new RuntimeException("Name is required");
        }
        if (user.getEmail() == null || user.getEmail().trim().isEmpty()) {
            throw new RuntimeException("Email is required");
        }
        if (user.getPasswordHash() == null || user.getPasswordHash().trim().isEmpty()) {
            throw new RuntimeException("Password is required for registered users");
        }
    }
}