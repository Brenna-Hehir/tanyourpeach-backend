package com.tanyourpeach.backend.util;

import com.tanyourpeach.backend.model.User;
import com.tanyourpeach.backend.repository.UserRepository;
import com.tanyourpeach.backend.service.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TestAuthHelper {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtService jwtService;

    public User createTestUser(String email, boolean isAdmin) {
        User user = new User();
        user.setName("Test User");
        user.setEmail(email);
        user.setPasswordHash("hashedpass");
        user.setAddress("123 Test Lane");
        user.setIsAdmin(isAdmin);
        return userRepository.save(user);
    }

    public String generateTokenFor(User user) {
        return jwtService.generateToken(user);
    }

    public String generateTokenFor(String email, boolean isAdmin) {
        User user = createTestUser(email, isAdmin);
        return generateTokenFor(user);
    }
}