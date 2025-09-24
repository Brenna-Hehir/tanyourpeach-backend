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

class FinancialLogValidationTest {

    private Validator validator;

    @BeforeEach
    void setup() {
        ValidatorFactory f = Validation.buildDefaultValidatorFactory();
        validator = f.getValidator();
    }

    private FinancialLog validLog() {
        FinancialLog f = new FinancialLog();
        f.setType(FinancialLog.Type.revenue);      // @NotNull
        f.setAmount(new BigDecimal("1.00"));       // @DecimalMin("0.01")
        f.setSource("walk-in");
        f.setDescription("ok");
        // logDate is DB-managed; not part of bean validation
        return f;
    }

    @Test
    @DisplayName("Missing type should fail")
    void missingType_shouldFail() {
        FinancialLog f = validLog();
        f.setType(null);

        Set<ConstraintViolation<FinancialLog>> v = validator.validate(f);
        assertFalse(v.isEmpty());
        assertTrue(v.stream().anyMatch(c -> c.getPropertyPath().toString().equals("type")));
    }

    @Test
    @DisplayName("Amount must be > 0.00")
    void amount_mustBePositive() {
        FinancialLog f1 = validLog();
        f1.setAmount(new BigDecimal("0.00"));
        Set<ConstraintViolation<FinancialLog>> v1 = validator.validate(f1);
        assertTrue(v1.stream().anyMatch(c -> c.getPropertyPath().toString().equals("amount")));

        FinancialLog f2 = validLog();
        f2.setAmount(new BigDecimal("-5.00"));
        Set<ConstraintViolation<FinancialLog>> v2 = validator.validate(f2);
        assertTrue(v2.stream().anyMatch(c -> c.getPropertyPath().toString().equals("amount")));
    }

    @Test
    @DisplayName("Source length must be ≤ 100")
    void source_length_limit() {
        FinancialLog f = validLog();
        f.setSource("x".repeat(101)); // 101 chars → violates @Size(max=100)
        Set<ConstraintViolation<FinancialLog>> v = validator.validate(f);
        assertTrue(v.stream().anyMatch(c -> c.getPropertyPath().toString().equals("source")));
    }

    @Test
    @DisplayName("Valid log should pass")
    void valid_shouldPass() {
        FinancialLog f = validLog();
        Set<ConstraintViolation<FinancialLog>> v = validator.validate(f);
        assertTrue(v.isEmpty());
    }
}