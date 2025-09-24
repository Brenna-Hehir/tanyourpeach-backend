package com.tanyourpeach.backend.model;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class AvailabilityValidationTest {

    private Validator validator;

    @BeforeEach
    void setup() {
        ValidatorFactory f = Validation.buildDefaultValidatorFactory();
        validator = f.getValidator();
    }

    private Availability validAvailability() {
        Availability a = new Availability();
        a.setDate(LocalDate.now().plusDays(7));   // @NotNull
        a.setStartTime(LocalTime.of(10, 0));      // @NotNull
        a.setEndTime(LocalTime.of(11, 0));        // @NotNull
        a.setIsBooked(false);
        a.setNotes("slot");
        return a;
    }

    @Test
    @DisplayName("Missing required fields should trigger violations")
    void missingRequiredFields_shouldFail() {
        Availability a = new Availability(); // intentionally empty
        Set<ConstraintViolation<Availability>> v = validator.validate(a);

        assertFalse(v.isEmpty());
        assertTrue(v.stream().anyMatch(c -> c.getPropertyPath().toString().equals("date")));
        assertTrue(v.stream().anyMatch(c -> c.getPropertyPath().toString().equals("startTime")));
        assertTrue(v.stream().anyMatch(c -> c.getPropertyPath().toString().equals("endTime")));
    }

    @Test
    @DisplayName("Fully valid availability should pass")
    void valid_shouldPass() {
        Availability a = validAvailability();
        Set<ConstraintViolation<Availability>> v = validator.validate(a);
        assertTrue(v.isEmpty());
    }
}