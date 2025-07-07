package com.tanurpeach.backend.service;

import com.tanurpeach.backend.model.User;
import com.tanurpeach.backend.repository.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
        return userRepository.save(user);
    }

    // PUT update user
    public Optional<User> updateUser(Long id, User updatedUser) {
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
}