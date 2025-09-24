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

class ServiceInventoryUsageValidationTest {

    private Validator validator;

    @BeforeEach
    void setup() {
        ValidatorFactory f = Validation.buildDefaultValidatorFactory();
        validator = f.getValidator();
    }

    private ServiceInventoryUsage usage(Long serviceId, Long itemId) {
        ServiceInventoryUsage u = new ServiceInventoryUsage();
        u.setId(new ServiceInventoryUsageKey(serviceId, itemId));
        // No @Valid on associations, so we don't need to populate service/item for bean validation
        return u;
    }

    @Test
    @DisplayName("Typical instance has no validation violations")
    void typical_shouldPassValidation() {
        ServiceInventoryUsage u = usage(1L, 2L);
        Set<ConstraintViolation<ServiceInventoryUsage>> v = validator.validate(u);
        assertTrue(v.isEmpty());
        assertEquals(1, u.getQuantityUsed()); // default from model
    }

    @Test
    @DisplayName("Null associations are allowed by current model (no bean constraints)")
    void null_associations_allowed() {
        ServiceInventoryUsage u = new ServiceInventoryUsage();
        u.setId(new ServiceInventoryUsageKey(1L, 2L));
        u.setService(null);
        u.setItem(null);
        Set<ConstraintViolation<ServiceInventoryUsage>> v = validator.validate(u);
        assertTrue(v.isEmpty());
    }

    @Test
    @DisplayName("quantityUsed is allowed to be null by current model")
    void quantityUsed_canBeNull_currentBehavior() {
        ServiceInventoryUsage u = usage(1L, 2L);
        u.setQuantityUsed(null);
        Set<ConstraintViolation<ServiceInventoryUsage>> v = validator.validate(u);
        assertTrue(v.isEmpty());
    }
}