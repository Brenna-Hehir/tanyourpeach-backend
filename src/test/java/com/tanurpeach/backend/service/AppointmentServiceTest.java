package com.tanurpeach.backend.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.tanyourpeach.backend.model.Appointment;
import com.tanyourpeach.backend.repository.AppointmentRepository;
import com.tanyourpeach.backend.service.AppointmentService;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.beans.factory.annotation.Autowired;

@SpringBootTest
class AppointmentServiceTest {

    @Autowired
    private AppointmentService appointmentService;

    @Test
    void contextLoads() {
        assertNotNull(appointmentService);
    }
}