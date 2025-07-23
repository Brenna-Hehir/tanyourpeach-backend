package com.tanyourpeach.backend.controller;

import com.tanyourpeach.backend.model.Availability;
import com.tanyourpeach.backend.service.AvailabilityService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class AvailabilityControllerTest {

    @Mock
    private AvailabilityService availabilityService;

    @InjectMocks
    private AvailabilityController availabilityController;

    private Availability testAvailability;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        testAvailability = new Availability();
        testAvailability.setSlotId(1L);
        testAvailability.setDate(LocalDate.of(2025, 7, 20));
        testAvailability.setStartTime(LocalTime.of(9, 0));
        testAvailability.setEndTime(LocalTime.of(10, 0));
    }

    @Test
    void getAllAvailabilities_shouldReturnList() {
        when(availabilityService.getAllAvailabilities()).thenReturn(List.of(testAvailability));

        List<Availability> result = availabilityController.getAllAvailabilities();
        assertEquals(1, result.size());
        assertEquals(testAvailability.getSlotId(), result.get(0).getSlotId());
    }

   @Test
    void getAvailableSlotsByDate_shouldReturnSlots() {
        LocalDate date = LocalDate.of(2025, 7, 20);
        when(availabilityService.getAvailableSlotsByDate(date)).thenReturn(List.of(testAvailability));

        ResponseEntity<?> response = availabilityController.getAvailableSlotsByDate("2025-07-20");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());

        List<?> result = (List<?>) response.getBody();
        assertEquals(1, result.size());

        Availability returned = (Availability) result.get(0);
        assertEquals(LocalTime.of(9, 0), returned.getStartTime());
    }

    @Test
    void getAvailableSlotsByDate_shouldReturn400_ifDateInvalid() {
        ResponseEntity<?> response = availabilityController.getAvailableSlotsByDate("not-a-date");
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Invalid date format", response.getBody());
    }

    @Test
    void createAvailability_shouldReturnCreatedSlot() {
        when(availabilityService.createAvailability(any())).thenReturn(testAvailability);

        ResponseEntity<Availability> response = availabilityController.createAvailability(testAvailability);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(testAvailability, response.getBody());
    }

    @Test
    void createAvailability_shouldReturn500_ifServiceFails() {
        when(availabilityService.createAvailability(any())).thenThrow(new RuntimeException("fail"));

        assertThrows(RuntimeException.class, () -> availabilityController.createAvailability(testAvailability));
    }

    @Test
    void updateAvailability_shouldReturnUpdatedSlot() {
        when(availabilityService.updateAvailability(eq(1L), any())).thenReturn(Optional.of(testAvailability));

        ResponseEntity<Availability> response = availabilityController.updateAvailability(1L, testAvailability);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(testAvailability, response.getBody());
    }

    @Test
    void updateAvailability_shouldReturn400_ifInvalidOrNotFound() {
        when(availabilityService.updateAvailability(eq(1L), any())).thenReturn(Optional.empty());

        ResponseEntity<Availability> response = availabilityController.updateAvailability(1L, testAvailability);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void deleteAvailability_shouldReturn204_ifDeleted() {
        when(availabilityService.deleteAvailability(1L)).thenReturn(true);

        ResponseEntity<Void> response = availabilityController.deleteAvailability(1L);
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
    }

    @Test
    void deleteAvailability_shouldReturn404_ifNotFound() {
        when(availabilityService.deleteAvailability(1L)).thenReturn(false);

        ResponseEntity<Void> response = availabilityController.deleteAvailability(1L);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }
}