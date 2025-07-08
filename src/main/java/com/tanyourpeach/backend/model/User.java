package com.tanyourpeach.backend.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

@Entity
@Table(name = "users")
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;

    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    private String passwordHash;

    private String address;

    private Boolean isAdmin;

    private LocalDateTime createdAt;

    @PrePersist
    public void onCreate() {
        createdAt = LocalDateTime.now();
    }

    // Getters and Setters

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Boolean getIsAdmin() {
        return isAdmin;
    }

    public void setIsAdmin(Boolean isAdmin) {
        this.isAdmin = isAdmin;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    // UserDetails interface methods

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        String role = Boolean.TRUE.equals(isAdmin) ? "ROLE_ADMIN" : "ROLE_USER";
        return Collections.singletonList(new SimpleGrantedAuthority(role));
    }

    @Override
    public String getUsername() {
        return email; // use email as the login username
    }

    @Override
    public String getPassword() {
        return passwordHash;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true; // modify if you implement expiration
    }

    @Override
    public boolean isAccountNonLocked() {
        return true; // modify if you implement locking
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true; // modify if you want credentials to expire
    }

    @Override
    public boolean isEnabled() {
        return true; // modify for disabled users if needed
    }
}
