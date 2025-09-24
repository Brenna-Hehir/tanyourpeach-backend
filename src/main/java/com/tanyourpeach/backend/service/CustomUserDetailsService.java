package com.tanyourpeach.backend.service;

import com.tanyourpeach.backend.model.User;
import com.tanyourpeach.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    // Load user by email for authentication
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        if (email == null) {
            throw new UsernameNotFoundException("User not found");
        }
        String normalizedEmail = email.trim().toLowerCase();
        if (normalizedEmail.isEmpty()) {
            throw new UsernameNotFoundException("User not found");
        }

        User user = userRepository.findByEmail(normalizedEmail)
            .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        // build authorities explicitly (roles will be prefixed with ROLE_ by convention)
        List<GrantedAuthority> authorities = List.of(
            new SimpleGrantedAuthority(user.getIsAdmin() ? "ROLE_ADMIN" : "ROLE_USER")
        );

        return org.springframework.security.core.userdetails.User
            .withUsername(user.getEmail())
            .password(user.getPasswordHash())
            .authorities(authorities)
            // Uncomment if you have these flags on your User entity and want to enforce them:
            // .accountLocked(!user.isAccountNonLocked())
            // .disabled(!user.isEnabled())
            .build();
    }
}