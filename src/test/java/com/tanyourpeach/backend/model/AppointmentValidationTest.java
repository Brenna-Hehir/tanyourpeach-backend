package com.tanyourpeach.backend.model;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class AppointmentValidationTest {

    private Validator validator;

    @BeforeEach
    void setup() {
        ValidatorFactory f = Validation.buildDefaultValidatorFactory();
        validator = f.getValidator();
    }

    // --- helpers ------------------------------------------------------------

    private TanService validService() {
        TanService s = new TanService();
        s.setServiceId(1L);       // avoid the debug print in setService(...)
        s.setName("Classic Tan");
        s.setBasePrice(50.0);
        s.setDurationMinutes(30);
        return s;
    }

    private Availability validAvailability() {
        Availability a = new Availability();
        a.setDate(LocalDate.now().plusDays(7));
        a.setStartTime(LocalTime.of(10, 0));
        a.setEndTime(LocalTime.of(11, 0));
        a.setIsBooked(false);
        a.setNotes("slot");
        return a;
    }

    private Appointment validAppointment() {
        Appointment ap = new Appointment();
        ap.setService(validService());
        ap.setAvailability(validAvailability());
        ap.setClientName("Test Client");
        ap.setClientEmail("client@example.com");
        ap.setClientAddress("123 Peach St");
        ap.setAppointmentDateTime(LocalDateTime.now().plusDays(7).withHour(10).withMinute(0));
        return ap;
    }

    // --- tests --------------------------------------------------------------

    @Test
    @DisplayName("Missing service should fail")
    void missingService_shouldFail() {
        Appointment ap = validAppointment();
        ap.setService(null);

        Set<ConstraintViolation<Appointment>> v = validator.validate(ap);
        assertTrue(v.stream().anyMatch(c -> c.getPropertyPath().toString().equals("service")));
    }

    @Test
    @DisplayName("Missing availability should fail")
    void missingAvailability_shouldFail() {
        Appointment ap = validAppointment();
        ap.setAvailability(null);

        Set<ConstraintViolation<Appointment>> v = validator.validate(ap);
        assertTrue(v.stream().anyMatch(c -> c.getPropertyPath().toString().equals("availability")));
    }

    @Test
    @DisplayName("Missing clientName should fail")
    void missingClientName_shouldFail() {
        Appointment ap = validAppointment();
        ap.setClientName(null);

        Set<ConstraintViolation<Appointment>> v = validator.validate(ap);
        assertTrue(v.stream().anyMatch(c -> c.getPropertyPath().toString().equals("clientName")));
    }

    @Test
    @DisplayName("Client name over 100 chars should fail")
    void clientName_tooLong_shouldFail() {
        Appointment ap = validAppointment();
        ap.setClientName("x".repeat(101));

        Set<ConstraintViolation<Appointment>> v = validator.validate(ap);
        assertTrue(v.stream().anyMatch(c -> c.getPropertyPath().toString().equals("clientName")));
    }

    @Test
    @DisplayName("Missing clientAddress should fail")
    void missingClientAddress_shouldFail() {
        Appointment ap = validAppointment();
        ap.setClientAddress(null);

        Set<ConstraintViolation<Appointment>> v = validator.validate(ap);
        assertTrue(v.stream().anyMatch(c -> c.getPropertyPath().toString().equals("clientAddress")));
    }

    @Test
    @DisplayName("Invalid clientEmail format should fail")
    void invalidEmail_shouldFail() {
        Appointment ap = validAppointment();
        ap.setClientEmail("not-an-email");

        Set<ConstraintViolation<Appointment>> v = validator.validate(ap);
        assertTrue(v.stream().anyMatch(c -> c.getPropertyPath().toString().equals("clientEmail")));
    }

    @Test
    @DisplayName("Missing appointmentDateTime should fail")
    void missingAppointmentDate_shouldFail() {
        Appointment ap = validAppointment();
        ap.setAppointmentDateTime(null);

        Set<ConstraintViolation<Appointment>> v = validator.validate(ap);
        assertTrue(v.stream().anyMatch(c -> c.getPropertyPath().toString().equals("appointmentDateTime")));
    }

    @Test
    @DisplayName("Fully valid appointment should pass")
    void valid_shouldPass() {
        Appointment ap = validAppointment();
        Set<ConstraintViolation<Appointment>> v = validator.validate(ap);
        assertTrue(v.isEmpty());
    }
}