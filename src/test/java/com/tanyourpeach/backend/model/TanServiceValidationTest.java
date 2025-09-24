package com.tanyourpeach.backend.model;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class TanServiceValidationTest {

    private Validator validator;

    @BeforeEach
    void setup() {
        ValidatorFactory f = Validation.buildDefaultValidatorFactory();
        validator = f.getValidator();
    }

    private TanService validService() {
        TanService s = new TanService();
        s.setName("Classic Tan");
        s.setBasePrice(50.0);      // @NotNull + @DecimalMin(inclusive=false)
        s.setDurationMinutes(30);  // @NotNull + @Min(1)
        s.setDescription("ok");
        return s;
    }

    @Test
    @DisplayName("Missing required fields should fail")
    void missingRequiredFields_shouldFail() {
        TanService s = new TanService(); // empty
        Set<ConstraintViolation<TanService>> v = validator.validate(s);
        assertFalse(v.isEmpty());
        assertTrue(v.stream().anyMatch(c -> c.getPropertyPath().toString().equals("name")));
        assertTrue(v.stream().anyMatch(c -> c.getPropertyPath().toString().equals("basePrice")));
        assertTrue(v.stream().anyMatch(c -> c.getPropertyPath().toString().equals("durationMinutes")));
    }

    @Test
    @DisplayName("Name over 100 chars should fail")
    void name_tooLong_shouldFail() {
        TanService s = validService();
        s.setName("x".repeat(101));
        Set<ConstraintViolation<TanService>> v = validator.validate(s);
        assertTrue(v.stream().anyMatch(c -> c.getPropertyPath().toString().equals("name")));
    }

    @Test
    @DisplayName("Base price must be > 0")
    void basePrice_positiveStrict() {
        TanService s1 = validService();
        s1.setBasePrice(0.0);
        assertTrue(validator.validate(s1).stream().anyMatch(c -> c.getPropertyPath().toString().equals("basePrice")));

        TanService s2 = validService();
        s2.setBasePrice(-5.0);
        assertTrue(validator.validate(s2).stream().anyMatch(c -> c.getPropertyPath().toString().equals("basePrice")));
    }

    @Test
    @DisplayName("Duration must be at least 1")
    void duration_minOne() {
        TanService s1 = validService();
        s1.setDurationMinutes(0);
        assertTrue(validator.validate(s1).stream().anyMatch(c -> c.getPropertyPath().toString().equals("durationMinutes")));
    }

    @Test
    @DisplayName("Valid service should pass")
    void valid_shouldPass() {
        assertTrue(validator.validate(validService()).isEmpty());
    }
}