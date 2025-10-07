package com.tanyourpeach.backend.service;

import com.tanyourpeach.backend.dto.UserCreateDto;
import com.tanyourpeach.backend.dto.UserUpdateDto;
import com.tanyourpeach.backend.model.User;
import com.tanyourpeach.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User getUserByIdOrThrow(Long id) {
        return userRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    }

    public User createUser(UserCreateDto dto) {
        String email = dto.getEmail().trim().toLowerCase();
        if (userRepository.findByEmail(email).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Duplicate resource");
        }

        User user = new User();
        user.setName(dto.getName().trim());
        user.setEmail(email);
        // If you hash passwords elsewhere, call that here instead:
        user.setPasswordHash(dto.getPassword().trim());
        user.setAddress(dto.getAddress());
        user.setIsAdmin(Boolean.TRUE.equals(dto.getIsAdmin()));

        return userRepository.save(user);
    }

    public User updateUser(Long id, UserUpdateDto dto) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        String newEmail = dto.getEmail().trim().toLowerCase();
        if (!user.getEmail().equalsIgnoreCase(newEmail)
                && userRepository.findByEmail(newEmail).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Duplicate resource");
        }

        user.setName(dto.getName().trim());
        user.setEmail(newEmail);
        user.setPasswordHash(dto.getPassword().trim()); // hash if applicable
        user.setAddress(dto.getAddress());
        user.setIsAdmin(Boolean.TRUE.equals(dto.getIsAdmin()));

        return userRepository.save(user);
    }

    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        }
        userRepository.deleteById(id);
    }
}