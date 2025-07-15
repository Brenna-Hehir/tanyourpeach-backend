package com.tanyourpeach.backend.service;

import com.tanyourpeach.backend.model.Availability;
import com.tanyourpeach.backend.repository.AvailabilityRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AvailabilityServiceTest {

    @Mock
    private AvailabilityRepository availabilityRepository;

    @InjectMocks
    private AvailabilityService availabilityService;

    private Availability availability;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        availability = new Availability();
        availability.setSlotId(1L);
        availability.setDate(LocalDate.of(2025, 8, 1));
        availability.setStartTime(LocalTime.of(10, 0));
        availability.setEndTime(LocalTime.of(11, 0));
        availability.setIsBooked(false);
        availability.setNotes("Available morning slot");
    }

    @Test
    void getAllAvailabilities_shouldReturnAll() {
        when(availabilityRepository.findAll()).thenReturn(List.of(availability));

        List<Availability> result = availabilityService.getAllAvailabilities();
        assertEquals(1, result.size());
        verify(availabilityRepository).findAll();
    }

    @Test
    void getAvailableSlotsByDate_shouldReturnOnlyUnbooked() {
        when(availabilityRepository.findByIsBookedFalseAndDate(availability.getDate()))
                .thenReturn(List.of(availability));

        List<Availability> result = availabilityService.getAvailableSlotsByDate(availability.getDate());
        assertEquals(1, result.size());
        assertFalse(result.get(0).getIsBooked());
    }

    @Test
    void createAvailability_shouldSaveAvailability() {
        when(availabilityRepository.save(any())).thenReturn(availability);

        Availability result = availabilityService.createAvailability(availability);
        assertNotNull(result);
        assertEquals(availability.getSlotId(), result.getSlotId());
    }

    @Test
    void updateAvailability_shouldUpdateFields() {
        when(availabilityRepository.findById(1L)).thenReturn(Optional.of(availability));
        when(availabilityRepository.save(any())).thenReturn(availability);

        Availability updated = new Availability();
        updated.setDate(LocalDate.of(2025, 8, 2));
        updated.setStartTime(LocalTime.of(12, 0));
        updated.setEndTime(LocalTime.of(13, 0));
        updated.setIsBooked(true);
        updated.setNotes("Updated time");

        Optional<Availability> result = availabilityService.updateAvailability(1L, updated);

        assertTrue(result.isPresent());
        assertEquals(LocalDate.of(2025, 8, 2), result.get().getDate());
        assertTrue(result.get().getIsBooked());
    }

    @Test
    void updateAvailability_shouldReturnEmptyIfNotFound() {
        when(availabilityRepository.findById(99L)).thenReturn(Optional.empty());

        Optional<Availability> result = availabilityService.updateAvailability(99L, availability);
        assertTrue(result.isEmpty());
    }

    @Test
    void deleteAvailability_shouldReturnTrueWhenExists() {
        when(availabilityRepository.existsById(1L)).thenReturn(true);

        boolean deleted = availabilityService.deleteAvailability(1L);
        assertTrue(deleted);
        verify(availabilityRepository).deleteById(1L);
    }

    @Test
    void deleteAvailability_shouldReturnFalseIfNotExists() {
        when(availabilityRepository.existsById(99L)).thenReturn(false);

        boolean deleted = availabilityService.deleteAvailability(99L);
        assertFalse(deleted);
    }
}