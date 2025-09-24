package com.tanyourpeach.backend.model;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class AppointmentStatusHistoryValidationTest {

    private Validator validator;

    @BeforeEach
    void setup() {
        ValidatorFactory f = Validation.buildDefaultValidatorFactory();
        validator = f.getValidator();
    }

    private AppointmentStatusHistory sample() {
        AppointmentStatusHistory h = new AppointmentStatusHistory();
        h.setAppointment(new Appointment());     // no @NotNull on field, so this is optional
        h.setStatus("CONFIRMED");                // no @NotBlank, but set it to reflect typical use
        h.setChangedAt(LocalDateTime.now());
        h.setchangedByEmail("client@example.com");
        return h;
    }

    @Test
    @DisplayName("Typical status history should have no validation violations (no bean constraints present)")
    void typical_shouldPass() {
        Set<ConstraintViolation<AppointmentStatusHistory>> v = validator.validate(sample());
        assertTrue(v.isEmpty());
    }
}