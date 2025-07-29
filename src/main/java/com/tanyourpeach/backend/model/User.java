package com.tanyourpeach.backend.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import java.util.Collection;
import java.util.Collections;

@Entity
@Table(name = "users")
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;

    @Size(max = 100, message = "Name must be under 100 characters")
    private String name;

    @Email(message = "Invalid email format")
    @NotBlank(message = "Email is required")
    @Column(unique = true, nullable = false)
    private String email;

    @NotBlank(message = "Password is required")
    @Column(name = "password_hash")
    private String passwordHash;

    @Column(columnDefinition = "TEXT")
    private String address;

    private Boolean isAdmin;

    private LocalDateTime createdAt;

    // Default constructor
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

    // This method returns the authorities granted to the user
    @Override
    @JsonIgnore
    public Collection<? extends GrantedAuthority> getAuthorities() {
        String role = Boolean.TRUE.equals(isAdmin) ? "ROLE_ADMIN" : "ROLE_USER";
        return Collections.singletonList(new SimpleGrantedAuthority(role));
    }

    // This method returns the username used for login
    // In this case, we use the email as the username
    @Override
    @JsonIgnore
    public String getUsername() {
        return email;
    }

    // This method returns the password hash stored in the database
    @Override
    @JsonIgnore
    public String getPassword() {
        return passwordHash;
    }

    // This method determines if the user's account is expired
    // Modify if you implement expiration logic
    @Override
    @JsonIgnore
    public boolean isAccountNonExpired() {
        return true;
    }

    // This method determines if the user's account is locked
    // Modify if you implement locking logic
    @Override
    @JsonIgnore
    public boolean isAccountNonLocked() {
        return true;
    }

    // This method determines if the user's credentials (password) are expired
    // Modify if you implement password expiration
    @Override
    @JsonIgnore
    public boolean isCredentialsNonExpired() {
        return true;
    }

    // This method determines if the user is enabled or disabled
    // Modify for disabled users if needed
    @Override
    @JsonIgnore
    public boolean isEnabled() {
        return true;
    }
}