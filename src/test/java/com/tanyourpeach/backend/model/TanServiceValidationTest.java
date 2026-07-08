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
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    private TanService validService() {
        TanService service = new TanService();
        service.setName("Classic Tan");
        service.setBasePrice(50.0);
        service.setDurationMinutes(30);
        service.setDescription("Standard spray tan service.");
        return service;
    }

    private void assertHasViolation(Set<ConstraintViolation<TanService>> violations, String propertyPath) {
        assertTrue(
                violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals(propertyPath)),
                "Expected violation for property path: " + propertyPath
        );
    }

    @Test
    @DisplayName("Missing required fields should fail")
    void missingRequiredFields_shouldFail() {
        TanService service = new TanService();

        Set<ConstraintViolation<TanService>> violations = validator.validate(service);

        assertFalse(violations.isEmpty());
        assertHasViolation(violations, "name");
        assertHasViolation(violations, "basePrice");
        assertHasViolation(violations, "durationMinutes");
    }

    @Test
    @DisplayName("Valid main service should pass")
    void validMainService_shouldPass() {
        TanService service = validService();

        assertTrue(validator.validate(service).isEmpty());
    }

    @Test
    @DisplayName("Name over 100 characters should fail")
    void name_tooLong_shouldFail() {
        TanService service = validService();
        service.setName("x".repeat(101));

        Set<ConstraintViolation<TanService>> violations = validator.validate(service);

        assertHasViolation(violations, "name");
    }

    @Test
    @DisplayName("Negative base price should fail")
    void basePrice_negative_shouldFail() {
        TanService service = validService();
        service.setBasePrice(-5.0);

        Set<ConstraintViolation<TanService>> violations = validator.validate(service);

        assertHasViolation(violations, "basePrice");
    }

    @Test
    @DisplayName("Negative duration should fail")
    void duration_negative_shouldFail() {
        TanService service = validService();
        service.setDurationMinutes(-1);

        Set<ConstraintViolation<TanService>> violations = validator.validate(service);

        assertHasViolation(violations, "durationMinutes");
    }

    @Test
    @DisplayName("Main service cannot have zero price")
    void mainService_zeroPrice_shouldFail() {
        TanService service = validService();
        service.setServiceType(ServiceType.MAIN_SERVICE);
        service.setBasePrice(0.0);

        Set<ConstraintViolation<TanService>> violations = validator.validate(service);

        assertHasViolation(violations, "mainServiceBasePriceValid");
    }

    @Test
    @DisplayName("Main service cannot have zero duration")
    void mainService_zeroDuration_shouldFail() {
        TanService service = validService();
        service.setServiceType(ServiceType.MAIN_SERVICE);
        service.setDurationMinutes(0);

        Set<ConstraintViolation<TanService>> violations = validator.validate(service);

        assertHasViolation(violations, "mainServiceDurationValid");
    }

    @Test
    @DisplayName("Add-on can have zero price and zero duration")
    void addOn_canHaveZeroPriceAndZeroDuration() {
        TanService service = validService();
        service.setServiceType(ServiceType.ADD_ON);
        service.setBasePrice(0.0);
        service.setDurationMinutes(0);

        assertTrue(validator.validate(service).isEmpty());
    }

    @Test
    @DisplayName("Service type is required")
    void serviceType_required_shouldFail() {
        TanService service = validService();
        service.setServiceType(null);

        Set<ConstraintViolation<TanService>> violations = validator.validate(service);

        assertHasViolation(violations, "serviceType");
    }

    @Test
    @DisplayName("Slug is optional")
    void slug_optional_shouldPass() {
        TanService service = validService();
        service.setSlug(null);

        assertTrue(validator.validate(service).isEmpty());
    }

    @Test
    @DisplayName("Valid slug should pass")
    void slug_validFormat_shouldPass() {
        TanService service = validService();
        service.setSlug("peach-cobbler");

        assertTrue(validator.validate(service).isEmpty());
    }

    @Test
    @DisplayName("Slug must use lowercase letters numbers and hyphens")
    void slug_invalidFormat_shouldFail() {
        TanService service = validService();
        service.setSlug("Peach Cobbler!");

        Set<ConstraintViolation<TanService>> violations = validator.validate(service);

        assertHasViolation(violations, "slug");
    }

    @Test
    @DisplayName("Blank slug should fail when provided")
    void slug_blank_shouldFail() {
        TanService service = validService();
        service.setSlug("");

        Set<ConstraintViolation<TanService>> violations = validator.validate(service);

        assertHasViolation(violations, "slug");
    }

    @Test
    @DisplayName("Slug over 150 characters should fail")
    void slug_tooLong_shouldFail() {
        TanService service = validService();
        service.setSlug("a".repeat(151));

        Set<ConstraintViolation<TanService>> violations = validator.validate(service);

        assertHasViolation(violations, "slug");
    }

    @Test
    @DisplayName("Short description is optional")
    void shortDescription_optional_shouldPass() {
        TanService service = validService();
        service.setShortDescription(null);

        assertTrue(validator.validate(service).isEmpty());
    }

    @Test
    @DisplayName("Blank short description should fail when provided")
    void shortDescription_blank_shouldFail() {
        TanService service = validService();
        service.setShortDescription("   ");

        Set<ConstraintViolation<TanService>> violations = validator.validate(service);

        assertHasViolation(violations, "shortDescriptionValid");
    }

    @Test
    @DisplayName("Short description over 255 characters should fail")
    void shortDescription_tooLong_shouldFail() {
        TanService service = validService();
        service.setShortDescription("x".repeat(256));

        Set<ConstraintViolation<TanService>> violations = validator.validate(service);

        assertHasViolation(violations, "shortDescription");
    }

    @Test
    @DisplayName("Card image URL is optional")
    void cardImageUrl_optional_shouldPass() {
        TanService service = validService();
        service.setCardImageUrl(null);

        assertTrue(validator.validate(service).isEmpty());
    }

    @Test
    @DisplayName("Blank card image URL should fail when provided")
    void cardImageUrl_blank_shouldFail() {
        TanService service = validService();
        service.setCardImageUrl("   ");

        Set<ConstraintViolation<TanService>> violations = validator.validate(service);

        assertHasViolation(violations, "cardImageUrlValid");
    }

    @Test
    @DisplayName("Card image URL over 500 characters should fail")
    void cardImageUrl_tooLong_shouldFail() {
        TanService service = validService();
        service.setCardImageUrl("x".repeat(501));

        Set<ConstraintViolation<TanService>> violations = validator.validate(service);

        assertHasViolation(violations, "cardImageUrl");
    }

    @Test
    @DisplayName("Hero image URL is optional")
    void heroImageUrl_optional_shouldPass() {
        TanService service = validService();
        service.setHeroImageUrl(null);

        assertTrue(validator.validate(service).isEmpty());
    }

    @Test
    @DisplayName("Blank hero image URL should fail when provided")
    void heroImageUrl_blank_shouldFail() {
        TanService service = validService();
        service.setHeroImageUrl("   ");

        Set<ConstraintViolation<TanService>> violations = validator.validate(service);

        assertHasViolation(violations, "heroImageUrlValid");
    }

    @Test
    @DisplayName("Hero image URL over 500 characters should fail")
    void heroImageUrl_tooLong_shouldFail() {
        TanService service = validService();
        service.setHeroImageUrl("x".repeat(501));

        Set<ConstraintViolation<TanService>> violations = validator.validate(service);

        assertHasViolation(violations, "heroImageUrl");
    }

    @Test
    @DisplayName("Display order is required")
    void displayOrder_required_shouldFail() {
        TanService service = validService();
        service.setDisplayOrder(null);

        Set<ConstraintViolation<TanService>> violations = validator.validate(service);

        assertHasViolation(violations, "displayOrder");
    }

    @Test
    @DisplayName("Display order cannot be negative")
    void displayOrder_negative_shouldFail() {
        TanService service = validService();
        service.setDisplayOrder(-1);

        Set<ConstraintViolation<TanService>> violations = validator.validate(service);

        assertHasViolation(violations, "displayOrder");
    }

    @Test
    @DisplayName("Display order zero should pass")
    void displayOrder_zero_shouldPass() {
        TanService service = validService();
        service.setDisplayOrder(0);

        assertTrue(validator.validate(service).isEmpty());
    }

    @Test
    @DisplayName("Rinse times are optional")
    void rinseTimes_optional_shouldPass() {
        TanService service = validService();
        service.setRinseTimeMinHours(null);
        service.setRinseTimeMaxHours(null);

        assertTrue(validator.validate(service).isEmpty());
    }

    @Test
    @DisplayName("Rinse min without max should fail")
    void rinseTimeMin_withoutMax_shouldFail() {
        TanService service = validService();
        service.setRinseTimeMinHours(8.0);
        service.setRinseTimeMaxHours(null);

        Set<ConstraintViolation<TanService>> violations = validator.validate(service);

        assertHasViolation(violations, "rinseTimePairValid");
    }

    @Test
    @DisplayName("Rinse max without min should fail")
    void rinseTimeMax_withoutMin_shouldFail() {
        TanService service = validService();
        service.setRinseTimeMinHours(null);
        service.setRinseTimeMaxHours(12.0);

        Set<ConstraintViolation<TanService>> violations = validator.validate(service);

        assertHasViolation(violations, "rinseTimePairValid");
    }

    @Test
    @DisplayName("Rinse min must be greater than zero")
    void rinseTimeMin_zero_shouldFail() {
        TanService service = validService();
        service.setRinseTimeMinHours(0.0);
        service.setRinseTimeMaxHours(12.0);

        Set<ConstraintViolation<TanService>> violations = validator.validate(service);

        assertHasViolation(violations, "rinseTimeMinHours");
    }

    @Test
    @DisplayName("Rinse max must be greater than zero")
    void rinseTimeMax_zero_shouldFail() {
        TanService service = validService();
        service.setRinseTimeMinHours(8.0);
        service.setRinseTimeMaxHours(0.0);

        Set<ConstraintViolation<TanService>> violations = validator.validate(service);

        assertHasViolation(violations, "rinseTimeMaxHours");
    }

    @Test
    @DisplayName("Rinse max must be greater than or equal to min")
    void rinseTimeMax_lessThanMin_shouldFail() {
        TanService service = validService();
        service.setRinseTimeMinHours(12.0);
        service.setRinseTimeMaxHours(8.0);

        Set<ConstraintViolation<TanService>> violations = validator.validate(service);

        assertHasViolation(violations, "rinseTimeRangeValid");
    }

    @Test
    @DisplayName("Rinse max equal to min should pass")
    void rinseTimeMax_equalToMin_shouldPass() {
        TanService service = validService();
        service.setRinseTimeMinHours(8.0);
        service.setRinseTimeMaxHours(8.0);

        assertTrue(validator.validate(service).isEmpty());
    }

    @Test
    @DisplayName("Valid rinse range should pass")
    void rinseTime_validRange_shouldPass() {
        TanService service = validService();
        service.setRinseTimeMinHours(8.0);
        service.setRinseTimeMaxHours(12.0);

        assertTrue(validator.validate(service).isEmpty());
    }
}
