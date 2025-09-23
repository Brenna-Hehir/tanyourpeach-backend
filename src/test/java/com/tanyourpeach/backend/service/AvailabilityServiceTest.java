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
import static org.mockito.ArgumentMatchers.any;
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
        availability.setDate(LocalDate.now().plusDays(10));
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
    void createAvailability_shouldReturnNull_whenDateIsInPast() {
        Availability pastSlot = new Availability();
        pastSlot.setDate(LocalDate.now().minusDays(1));
        pastSlot.setStartTime(LocalTime.of(9, 0));
        pastSlot.setEndTime(LocalTime.of(10, 0));

        Availability result = availabilityService.createAvailability(pastSlot);
        assertNull(result);
    }

    @Test
    void createAvailability_shouldReturnNull_ifTimeOverlaps() {
        Availability existingSlot = new Availability();
        existingSlot.setSlotId(1L);
        existingSlot.setDate(LocalDate.now().plusDays(10));
        existingSlot.setStartTime(LocalTime.of(10, 0));
        existingSlot.setEndTime(LocalTime.of(11, 0));

        Availability newSlot = new Availability();
        newSlot.setDate(LocalDate.now().plusDays(10));
        newSlot.setStartTime(LocalTime.of(10, 30)); // overlaps
        newSlot.setEndTime(LocalTime.of(11, 30));

        when(availabilityRepository.findByDate(LocalDate.now().plusDays(10))).thenReturn(List.of(existingSlot));

        Availability result = availabilityService.createAvailability(newSlot);
        assertNull(result);
        verify(availabilityRepository, never()).save(any());
    }

    @Test
    void createAvailability_shouldReturnNull_ifEndTimeNotAfterStartTime() {
        Availability invalidSlot = new Availability();
        invalidSlot.setDate(LocalDate.now().plusDays(11));
        invalidSlot.setStartTime(LocalTime.of(15, 0));
        invalidSlot.setEndTime(LocalTime.of(15, 0)); // not after

        Availability result = availabilityService.createAvailability(invalidSlot);
        assertNull(result);
        verify(availabilityRepository, never()).save(any());
    }

    @Test
    void updateAvailability_shouldUpdateFields() {
        when(availabilityRepository.findById(1L)).thenReturn(Optional.of(availability));
        when(availabilityRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Availability updated = new Availability();
        updated.setDate(LocalDate.now().plusDays(11));
        updated.setStartTime(LocalTime.of(12, 0));
        updated.setEndTime(LocalTime.of(13, 0));
        updated.setIsBooked(true);
        updated.setNotes("Updated time");

        Optional<Availability> result = availabilityService.updateAvailability(1L, updated);

        assertTrue(result.isPresent());
        assertEquals(updated.getDate(), result.get().getDate());
        assertTrue(result.get().getIsBooked());
    }

    @Test
    void updateAvailability_shouldUpdateAllFieldsCorrectly() {
        when(availabilityRepository.findById(1L)).thenReturn(Optional.of(availability));
        when(availabilityRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        Availability updated = new Availability();
        updated.setDate(LocalDate.now().plusDays(12));
        updated.setStartTime(LocalTime.of(13, 0));
        updated.setEndTime(LocalTime.of(14, 0));
        updated.setIsBooked(true);
        updated.setNotes("Updated slot");

        Optional<Availability> result = availabilityService.updateAvailability(1L, updated);

        assertTrue(result.isPresent());
        Availability resultSlot = result.get();
        assertEquals(LocalDate.now().plusDays(12), resultSlot.getDate());
        assertEquals(LocalTime.of(13, 0), resultSlot.getStartTime());
        assertEquals(LocalTime.of(14, 0), resultSlot.getEndTime());
        assertTrue(resultSlot.getIsBooked());
        assertEquals("Updated slot", resultSlot.getNotes());
    }

    @Test
    void updateAvailability_shouldReturnEmptyIfNotFound() {
        when(availabilityRepository.findById(99L)).thenReturn(Optional.empty());

        Optional<Availability> result = availabilityService.updateAvailability(99L, availability);
        assertTrue(result.isEmpty());
    }

    @Test
    void updateAvailability_shouldReturnEmpty_whenDateIsInPast() {
        when(availabilityRepository.findById(1L)).thenReturn(Optional.of(availability));

        Availability updated = new Availability();
        updated.setDate(LocalDate.now().minusDays(1));
        updated.setStartTime(LocalTime.of(10, 0));
        updated.setEndTime(LocalTime.of(11, 0));
        updated.setIsBooked(false);

        Optional<Availability> result = availabilityService.updateAvailability(1L, updated);
        assertTrue(result.isEmpty());
    }

    @Test
    void updateAvailability_shouldReturnEmpty_ifTimeOverlaps() {
        Availability existingSlot = new Availability();
        existingSlot.setSlotId(2L); // another slot on same day
        existingSlot.setDate(LocalDate.now().plusDays(10));
        existingSlot.setStartTime(LocalTime.of(12, 0));
        existingSlot.setEndTime(LocalTime.of(13, 0));

        when(availabilityRepository.findById(1L)).thenReturn(Optional.of(availability));
        when(availabilityRepository.findByDate(LocalDate.now().plusDays(10))).thenReturn(List.of(availability, existingSlot));

        Availability updated = new Availability();
        updated.setDate(LocalDate.now().plusDays(10));
        updated.setStartTime(LocalTime.of(12, 30)); // overlaps with slotId 2
        updated.setEndTime(LocalTime.of(13, 30));
        updated.setIsBooked(false);

        Optional<Availability> result = availabilityService.updateAvailability(1L, updated);
        assertTrue(result.isEmpty());
        verify(availabilityRepository, never()).save(any());
    }

    @Test
    void updateAvailability_shouldReturnEmpty_ifEndTimeNotAfterStartTime() {
        when(availabilityRepository.findById(1L)).thenReturn(Optional.of(availability));

        Availability updated = new Availability();
        updated.setDate(LocalDate.now().plusDays(10));
        updated.setStartTime(LocalTime.of(14, 0));
        updated.setEndTime(LocalTime.of(14, 0)); // invalid
        updated.setIsBooked(false);

        Optional<Availability> result = availabilityService.updateAvailability(1L, updated);
        assertTrue(result.isEmpty());
        verify(availabilityRepository, never()).save(any());
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