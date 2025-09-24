package com.tanyourpeach.backend.model;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class ReceiptValidationTest {

    private Validator validator;

    @BeforeEach
    void setup() {
        ValidatorFactory f = Validation.buildDefaultValidatorFactory();
        validator = f.getValidator();
    }

    private Receipt sampleReceipt() {
        Receipt r = new Receipt();
        r.setTotalAmount(new BigDecimal("75.00"));
        r.setPaymentMethod("CARD");
        r.setNotes("ok");
        // appointment/dateIssued are not validated by annotations in your model snippet
        return r;
    }

    @Test
    @DisplayName("Typical receipt should have no validation violations")
    void typicalReceipt_shouldPass() {
        Receipt r = sampleReceipt();
        Set<ConstraintViolation<Receipt>> v = validator.validate(r);
        assertTrue(v.isEmpty());
    }
}