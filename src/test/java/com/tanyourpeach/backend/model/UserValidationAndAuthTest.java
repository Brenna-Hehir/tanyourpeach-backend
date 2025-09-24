package com.tanyourpeach.backend.model;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class UserValidationAndAuthTest {

    private Validator validator;

    @BeforeEach
    void setup() {
        ValidatorFactory f = Validation.buildDefaultValidatorFactory();
        validator = f.getValidator();
    }

    private User validUser() {
        User u = new User();
        u.setName("Test User");                // @Size(max=100) only (optional)
        u.setEmail("user@example.com");        // @NotBlank + @Email
        u.setPasswordHash("hashed");           // @NotBlank
        u.setIsAdmin(false);
        return u;
    }

    // ---------- Bean validation ----------

    @Test
    @DisplayName("Missing email should fail")
    void missingEmail_shouldFail() {
        User u = validUser();
        u.setEmail(null);
        Set<ConstraintViolation<User>> v = validator.validate(u);
        assertTrue(v.stream().anyMatch(c -> c.getPropertyPath().toString().equals("email")));
    }

    @Test
    @DisplayName("Invalid email should fail")
    void invalidEmail_shouldFail() {
        User u = validUser();
        u.setEmail("not-an-email");
        Set<ConstraintViolation<User>> v = validator.validate(u);
        assertTrue(v.stream().anyMatch(c -> c.getPropertyPath().toString().equals("email")));
    }

    @Test
    @DisplayName("Missing passwordHash should fail")
    void missingPassword_shouldFail() {
        User u = validUser();
        u.setPasswordHash("   ");
        Set<ConstraintViolation<User>> v = validator.validate(u);
        assertTrue(v.stream().anyMatch(c -> c.getPropertyPath().toString().equals("passwordHash")));
    }

    @Test
    @DisplayName("Name over 100 chars should fail (max=100)")
    void name_tooLong_shouldFail() {
        User u = validUser();
        u.setName("x".repeat(101));
        Set<ConstraintViolation<User>> v = validator.validate(u);
        assertTrue(v.stream().anyMatch(c -> c.getPropertyPath().toString().equals("name")));
    }

    @Test
    @DisplayName("Null name is allowed by current model")
    void name_null_allowed() {
        User u = validUser();
        u.setName(null); // @Size only → null is allowed
        assertTrue(validator.validate(u).isEmpty());
    }

    @Test
    @DisplayName("Valid user should pass")
    void valid_shouldPass() {
        assertTrue(validator.validate(validUser()).isEmpty());
    }

    // ---------- Spring Security mapping ----------

    @Test
    @DisplayName("Authorities: isAdmin=true → ROLE_ADMIN")
    void authorities_admin() {
        User admin = validUser();
        admin.setIsAdmin(true);
        var roles = admin.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList();
        assertTrue(roles.contains("ROLE_ADMIN"));
        assertFalse(roles.contains("ROLE_USER"));
        assertEquals("user@example.com", admin.getUsername());
        assertEquals("hashed", admin.getPassword());
        assertTrue(admin.isAccountNonExpired());
        assertTrue(admin.isAccountNonLocked());
        assertTrue(admin.isCredentialsNonExpired());
        assertTrue(admin.isEnabled());
    }

    @Test
    @DisplayName("Authorities: isAdmin=false/null → ROLE_USER")
    void authorities_user_whenFalseOrNull() {
        User u1 = validUser();       // isAdmin=false
        var r1 = u1.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList();
        assertTrue(r1.contains("ROLE_USER"));

        User u2 = validUser();       // isAdmin=null
        u2.setIsAdmin(null);
        var r2 = u2.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList();
        assertTrue(r2.contains("ROLE_USER"));
    }
}