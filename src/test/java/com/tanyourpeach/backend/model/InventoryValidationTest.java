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

class InventoryValidationTest {

    private Validator validator;

    @BeforeEach
    void setup() {
        ValidatorFactory f = Validation.buildDefaultValidatorFactory();
        validator = f.getValidator();
    }

    private Inventory validItem() {
        Inventory i = new Inventory();
        i.setItemName("Caps");                              // @NotBlank, @Size(<=100)
        i.setQuantity(3);                                   // @Min(0)
        i.setUnitCost(new BigDecimal("0.20"));              // @DecimalMin(value="0.00", inclusive=false)
        i.setTotalSpent(new BigDecimal("0.00"));            // @DecimalMin(value="0.00")
        i.setLowStockThreshold(5);                          // @Min(0)
        i.setNotes("ok");
        return i;
    }

    @Test
    @DisplayName("Missing/blank name should fail")
    void name_required() {
        Inventory i = validItem();
        i.setItemName("   ");
        Set<ConstraintViolation<Inventory>> v = validator.validate(i);
        assertTrue(v.stream().anyMatch(c -> c.getPropertyPath().toString().equals("itemName")));
    }

    @Test
    @DisplayName("Quantity cannot be negative")
    void quantity_nonNegative() {
        Inventory i = validItem();
        i.setQuantity(-1);
        Set<ConstraintViolation<Inventory>> v = validator.validate(i);
        assertTrue(v.stream().anyMatch(c -> c.getPropertyPath().toString().equals("quantity")));
    }

    @Test
    @DisplayName("Unit cost must be > 0")
    void unitCost_positiveStrict() {
        Inventory i1 = validItem();
        i1.setUnitCost(new BigDecimal("0.00"));
        Set<ConstraintViolation<Inventory>> v1 = validator.validate(i1);
        assertTrue(v1.stream().anyMatch(c -> c.getPropertyPath().toString().equals("unitCost")));

        Inventory i2 = validItem();
        i2.setUnitCost(new BigDecimal("-1.00"));
        Set<ConstraintViolation<Inventory>> v2 = validator.validate(i2);
        assertTrue(v2.stream().anyMatch(c -> c.getPropertyPath().toString().equals("unitCost")));
    }

    @Test
    @DisplayName("Total spent cannot be negative")
    void totalSpent_nonNegative() {
        Inventory i = validItem();
        i.setTotalSpent(new BigDecimal("-0.01"));
        Set<ConstraintViolation<Inventory>> v = validator.validate(i);
        assertTrue(v.stream().anyMatch(c -> c.getPropertyPath().toString().equals("totalSpent")));
    }

    @Test
    @DisplayName("Low stock threshold cannot be negative")
    void threshold_nonNegative() {
        Inventory i = validItem();
        i.setLowStockThreshold(-2);
        Set<ConstraintViolation<Inventory>> v = validator.validate(i);
        assertTrue(v.stream().anyMatch(c -> c.getPropertyPath().toString().equals("lowStockThreshold")));
    }

    @Test
    @DisplayName("Valid inventory should pass")
    void valid_shouldPass() {
        Inventory i = validItem();
        Set<ConstraintViolation<Inventory>> v = validator.validate(i);
        assertTrue(v.isEmpty());
    }

    @Test
    @DisplayName("Item name over 100 chars should fail")
    void itemName_tooLong_shouldFail() {
        Inventory i = validItem();
        i.setItemName("x".repeat(101));
        var v = validator.validate(i);
        assertTrue(v.stream().anyMatch(c -> c.getPropertyPath().toString().equals("itemName")));
    }

    @Test
    @DisplayName("unitCost is optional (null allowed); when present it must be > 0")
    void unitCost_null_allowed() {
        Inventory i = validItem();
        i.setUnitCost(null); // optional by model (no @NotNull)
        var v = validator.validate(i);
        assertTrue(v.isEmpty());
    }
}