package com.tanyourpeach.backend.service;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import com.tanyourpeach.backend.model.User;
import io.jsonwebtoken.io.Decoders;

import java.security.Key;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private JwtService jwtService;
    private Key key;
    private User appUser;
    private org.springframework.security.core.userdetails.UserDetails springUser;

    private Key getKeyUsedByJwtService() {
        byte[] keyBytes = Decoders.BASE64.decode("MzI3NjM0NzVENEY2NDU1NzY4NTY2QjU5NzAzMzczMzY3NjM5NzkyNDQyMjY0NTI5NDg0MDRENjM1MTY2NTQ2QQ==");
        return Keys.hmacShaKeyFor(keyBytes);
    }

    @BeforeEach
    void setup() {
        jwtService = new JwtService();

        // This should match the SECRET_KEY in JwtService
        key = Keys.hmacShaKeyFor(Decoders.BASE64.decode("MzI3NjM0NzVENEY2NDU1NzY4NTY2QjU5NzAzMzczMzY3NjM5NzkyNDQyMjY0NTI5NDg0MDRENjM1MTY2NTQ2QQ=="));

        // Your model User
        appUser = new User();
        appUser.setEmail("user@example.com");
        appUser.setPasswordHash("password");

        // Spring Security UserDetails version (used for validation)
        springUser = org.springframework.security.core.userdetails.User
                .withUsername("user@example.com")
                .password("password")
                .roles("USER")
                .build();
    }

    @Test
    void generateToken_shouldIncludeEmailAndAdminClaim() {
        User adminUser = new User();
        adminUser.setEmail("admin@example.com");
        adminUser.setIsAdmin(true);

        String token = jwtService.generateToken(adminUser);
        String email = jwtService.extractUsername(token);
        Boolean isAdmin = jwtService.extractClaim(token, claims -> claims.get("isAdmin", Boolean.class));

        assertEquals("admin@example.com", email);
        assertTrue(isAdmin);
    }

    @Test
    void extractToken_shouldReturnToken_whenHeaderValid() {
        var request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer test.token.value");

        String extracted = jwtService.extractToken(request);
        assertEquals("test.token.value", extracted);
    }

    @Test
    void extractToken_shouldReturnNull_whenHeaderMissingOrInvalid() {
        var noHeader = new MockHttpServletRequest();
        var badHeader = new MockHttpServletRequest();
        badHeader.addHeader("Authorization", "Token something");

        assertNull(jwtService.extractToken(noHeader));
        assertNull(jwtService.extractToken(badHeader));
    }

    @Test
    void extractUsername_shouldReturnNull_whenTokenMalformed() {
        String malformed = "invalid.token.value";
        assertNull(jwtService.extractUsername(malformed));
    }

    @Test
    void tokenJustBeforeExpiration_shouldStillBeValid() {
        long now = System.currentTimeMillis();
        String token = Jwts.builder()
                .setSubject("user@example.com")
                .setIssuedAt(new Date(now - 1000 * 60 * 60))
                .setExpiration(new Date(now + 5000)) // give a 5 second buffer
                .signWith(getKeyUsedByJwtService(), SignatureAlgorithm.HS256)
                .compact();

        boolean valid = jwtService.isTokenValid(token, springUser);
        assertTrue(valid);
    }

    @Test
    void futureIssuedToken_shouldBeValid_butMayRaiseConcern() {
        String token = Jwts.builder()
                .setSubject("future@example.com")
                .setIssuedAt(new Date(System.currentTimeMillis() + 1000 * 60 * 60)) // 1hr in future
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 2))
                .signWith(getKeyUsedByJwtService(), SignatureAlgorithm.HS256)
                .compact();

        assertDoesNotThrow(() -> jwtService.extractUsername(token));
    }

    @Test
    void tokenMissingSubject_shouldFailExtractUsername() {
        String token = Jwts.builder()
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 100000))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();

        assertNull(jwtService.extractUsername(token));
    }

    @Test
    void expiredToken_shouldBeInvalid() {
        String token = Jwts.builder()
                .setSubject("user@example.com")
                .setIssuedAt(new Date(System.currentTimeMillis() - 1000 * 60 * 60 * 2)) // 2 hours ago
                .setExpiration(new Date(System.currentTimeMillis() - 1000 * 60 * 60)) // 1 hour ago
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();

        boolean valid = false;
        try {
            valid = jwtService.isTokenValid(token, springUser);
        } catch (Exception ignored) {}

        assertFalse(valid);
    }

    @Test
    void tamperedToken_shouldBeInvalid() {
        String token = jwtService.generateToken(appUser);
        // Remove signature part completely (split by '.')
        String[] parts = token.split("\\.");
        String tampered = parts[0] + "." + parts[1] + ".invalidsignature";

        boolean valid = false;
        try {
            valid = jwtService.isTokenValid(tampered, springUser);
        } catch (Exception ignored) {}

        assertFalse(valid);
    }

    @Test
    void malformedToken_shouldBeInvalid() {
        String malformed = "not.a.jwt";
        assertThrows(Exception.class, () -> jwtService.isTokenValid(malformed, springUser));
    }

    @Test
    void tokenWithDifferentSignature_shouldBeInvalid() {
        Key differentKey = Keys.secretKeyFor(SignatureAlgorithm.HS256);

        String token = Jwts.builder()
                .setSubject("spoof@example.com")
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60))
                .signWith(differentKey, SignatureAlgorithm.HS256)
                .compact();

        boolean valid = false;
        try {
            valid = jwtService.isTokenValid(token, springUser);
        } catch (Exception ignored) {}

        assertFalse(valid);
    }
}