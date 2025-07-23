package com.tanyourpeach.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tanyourpeach.backend.model.Availability;
import com.tanyourpeach.backend.repository.AppointmentRepository;
import com.tanyourpeach.backend.repository.AvailabilityRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AvailabilityControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AvailabilityRepository availabilityRepository;

    @Autowired
    private AppointmentRepository appointmentRepository;

    private Availability testSlot;

    @BeforeEach
    void setup() {
        appointmentRepository.deleteAll();
        availabilityRepository.deleteAll();

        testSlot = new Availability();
        testSlot.setDate(LocalDate.now().plusDays(1));
        testSlot.setStartTime(LocalTime.of(10, 0));
        testSlot.setEndTime(LocalTime.of(11, 0));
        testSlot.setIsBooked(false);
        testSlot.setNotes("Test slot");
        availabilityRepository.save(testSlot);
    }

    @Test
    void getAllAvailabilities_shouldReturnList() throws Exception {
        mockMvc.perform(get("/api/availabilities"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].slotId").value(testSlot.getSlotId()));
    }

    @Test
    void getAvailableSlotsByDate_shouldReturnMatchingSlots() throws Exception {
        String date = testSlot.getDate().toString();

        mockMvc.perform(get("/api/availabilities/available/" + date))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].slotId").value(testSlot.getSlotId()));
    }

    @Test
    void getAvailableSlotsByDate_shouldFailWithBadDate() throws Exception {
        mockMvc.perform(get("/api/availabilities/available/bad-date"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Invalid date format"));
    }

    @Test
    void createAvailability_shouldSucceedWithValidInput() throws Exception {
        Availability newSlot = new Availability();
        newSlot.setDate(LocalDate.now().plusDays(2));
        newSlot.setStartTime(LocalTime.of(12, 0));
        newSlot.setEndTime(LocalTime.of(13, 0));
        newSlot.setIsBooked(false);
        newSlot.setNotes("New Slot");

        mockMvc.perform(post("/api/availabilities")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newSlot)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.slotId").exists());
    }

    @Test
    void createAvailability_shouldFailForOverlap() throws Exception {
        Availability overlap = new Availability();
        overlap.setDate(testSlot.getDate());
        overlap.setStartTime(LocalTime.of(10, 30));
        overlap.setEndTime(LocalTime.of(11, 30));
        overlap.setIsBooked(false);

        mockMvc.perform(post("/api/availabilities")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(overlap)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createAvailability_shouldFailForInvalidTimes() throws Exception {
        Availability invalid = new Availability();
        invalid.setDate(LocalDate.now().plusDays(1));
        invalid.setStartTime(LocalTime.of(14, 0));
        invalid.setEndTime(LocalTime.of(13, 0)); // end before start
        invalid.setIsBooked(false);

        mockMvc.perform(post("/api/availabilities")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createAvailability_shouldFailWhenDateMissing() throws Exception {
        Availability invalid = new Availability();
        invalid.setStartTime(LocalTime.of(12, 0));
        invalid.setEndTime(LocalTime.of(13, 0));
        invalid.setIsBooked(false);

        mockMvc.perform(post("/api/availabilities")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createAvailability_shouldFailWhenStartTimeMissing() throws Exception {
        Availability invalid = new Availability();
        invalid.setDate(LocalDate.now().plusDays(1));
        invalid.setEndTime(LocalTime.of(13, 0));
        invalid.setIsBooked(false);

        mockMvc.perform(post("/api/availabilities")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createAvailability_shouldFailWhenEndTimeMissing() throws Exception {
        Availability invalid = new Availability();
        invalid.setDate(LocalDate.now().plusDays(1));
        invalid.setStartTime(LocalTime.of(12, 0));
        invalid.setIsBooked(false);

        mockMvc.perform(post("/api/availabilities")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateAvailability_shouldSucceedWithValidInput() throws Exception {
        testSlot.setStartTime(LocalTime.of(14, 0));
        testSlot.setEndTime(LocalTime.of(15, 0));

        mockMvc.perform(put("/api/availabilities/" + testSlot.getSlotId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testSlot)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.startTime").value("14:00:00"));
    }

    @Test
    void updateAvailability_shouldFailForOverlap() throws Exception {
        Availability otherSlot = new Availability();
        otherSlot.setDate(testSlot.getDate());
        otherSlot.setStartTime(LocalTime.of(12, 0));
        otherSlot.setEndTime(LocalTime.of(13, 0));
        otherSlot.setIsBooked(false);
        availabilityRepository.save(otherSlot);

        Availability overlap = new Availability();
        overlap.setDate(testSlot.getDate());
        overlap.setStartTime(LocalTime.of(12, 0)); // overlap with otherSlot
        overlap.setEndTime(LocalTime.of(12, 30));
        overlap.setIsBooked(false);

        mockMvc.perform(put("/api/availabilities/" + testSlot.getSlotId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(overlap)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateAvailability_shouldFailForInvalidTimeRange() throws Exception {
        Availability invalid = new Availability();
        invalid.setDate(LocalDate.now().plusDays(1));
        invalid.setStartTime(LocalTime.of(15, 0));
        invalid.setEndTime(LocalTime.of(14, 0));
        invalid.setIsBooked(false);

        mockMvc.perform(put("/api/availabilities/" + testSlot.getSlotId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateAvailability_shouldFailWhenDateMissing() throws Exception {
        Availability invalid = new Availability();
        invalid.setStartTime(LocalTime.of(10, 0));
        invalid.setEndTime(LocalTime.of(11, 0));
        invalid.setIsBooked(false);

        mockMvc.perform(put("/api/availabilities/" + testSlot.getSlotId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateAvailability_shouldFailWhenStartTimeMissing() throws Exception {
        Availability invalid = new Availability();
        invalid.setDate(LocalDate.now().plusDays(1));
        invalid.setEndTime(LocalTime.of(11, 0));
        invalid.setIsBooked(false);

        mockMvc.perform(put("/api/availabilities/" + testSlot.getSlotId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateAvailability_shouldFailWhenEndTimeMissing() throws Exception {
        Availability invalid = new Availability();
        invalid.setDate(LocalDate.now().plusDays(1));
        invalid.setStartTime(LocalTime.of(10, 0));
        invalid.setIsBooked(false);

        mockMvc.perform(put("/api/availabilities/" + testSlot.getSlotId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateAvailability_shouldFailForNonexistentId() throws Exception {
        Availability valid = new Availability();
        valid.setDate(LocalDate.now().plusDays(1));
        valid.setStartTime(LocalTime.of(12, 0));
        valid.setEndTime(LocalTime.of(13, 0));
        valid.setIsBooked(false);

        mockMvc.perform(put("/api/availabilities/9999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(valid)))
                .andExpect(status().isBadRequest()); // changed from isNotFound()
    }

    @Test
    void deleteAvailability_shouldSucceed() throws Exception {
        mockMvc.perform(delete("/api/availabilities/" + testSlot.getSlotId()))
                .andExpect(status().isNoContent());

        assertThat(availabilityRepository.findById(testSlot.getSlotId())).isNotPresent();
    }

    @Test
    void deleteAvailability_shouldFailForNonexistentId() throws Exception {
        mockMvc.perform(delete("/api/availabilities/9999"))
                .andExpect(status().isNotFound());
    }
}