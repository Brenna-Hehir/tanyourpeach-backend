package com.tanyourpeach.backend.repository;

import com.tanyourpeach.backend.model.Appointment;
import com.tanyourpeach.backend.model.Availability;
import com.tanyourpeach.backend.model.Receipt;
import com.tanyourpeach.backend.model.TanService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.TestPropertySource;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:receiptrepo;DB_CLOSE_DELAY=-1;MODE=MySQL;DATABASE_TO_UPPER=false",
        "spring.datasource.driverClassName=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect",
        "spring.sql.init.mode=never",
        "spring.flyway.enabled=false",
        "spring.liquibase.enabled=false",
        "spring.cloud.gcp.secretmanager.enabled=false",
        "spring.cloud.gcp.sql.enabled=false"
})
class ReceiptRepositoryTest {

    @Autowired private ReceiptRepository receiptRepository;
    @Autowired private AppointmentRepository appointmentRepository;
    @Autowired private AvailabilityRepository availabilityRepository;
    @Autowired private TanServiceRepository tanServiceRepository;

    private TanService svc(String name, String price) {
        TanService s = new TanService();
        s.setName(name);
        s.setBasePrice(Double.valueOf(price));
        s.setDurationMinutes(30); 
        return s;
    }

    private Availability slot(LocalDate d, int sh, int sm, int eh, int em) {
        Availability a = new Availability();
        a.setDate(d);
        a.setStartTime(LocalTime.of(sh, sm));
        a.setEndTime(LocalTime.of(eh, em));
        a.setIsBooked(false);
        a.setNotes("slot");
        return a;
    }

    private Appointment appt(TanService s, Availability a, String email, String addr, LocalDateTime when) {
        Appointment ap = new Appointment();
        ap.setService(s);
        ap.setAvailability(a);
        ap.setClientName("Test Client");
        ap.setClientEmail(email);
        ap.setClientAddress(addr);
        ap.setAppointmentDateTime(when);
        return ap;
    }

    private Receipt rcpt(Appointment ap, String total, String method) {
        Receipt r = new Receipt();
        r.setAppointment(ap);
        r.setTotalAmount(new BigDecimal(total));
        r.setPaymentMethod(method);
        return r;
    }

    @Test
    @DisplayName("findByAppointment_AppointmentId: returns the linked receipt")
    void findByAppointmentId_returnsReceipt() {
        TanService s = tanServiceRepository.save(svc("Classic", "50.00"));
        Availability a = availabilityRepository.save(
                slot(LocalDate.now().plusDays(7), 10, 0, 11, 0)
        );
        Appointment ap = appointmentRepository.save(
                appt(s, a, "client@example.com", "123 Peach St",
                        LocalDateTime.now().plusDays(7).withHour(10).withMinute(0))
        );

        Receipt r = receiptRepository.save(rcpt(ap, "75.00", "CARD"));

        Receipt found = receiptRepository.findByAppointment_AppointmentId(ap.getAppointmentId());
        assertNotNull(found);
        assertEquals(r.getReceiptId(), found.getReceiptId()); // <- use receiptId
        assertEquals(new BigDecimal("75.00"), found.getTotalAmount());
        assertEquals("CARD", found.getPaymentMethod());
    }

    @Test
    @DisplayName("sumTotalRevenue: sums all receipt totals")
    void sumTotalRevenue_sumsAll() {
        TanService s = tanServiceRepository.save(svc("Rapid", "60.00"));
        Availability a1 = availabilityRepository.save(slot(LocalDate.now().plusDays(5), 12, 0, 13, 0));
        Availability a2 = availabilityRepository.save(slot(LocalDate.now().plusDays(6), 14, 0, 15, 0));

        Appointment ap1 = appointmentRepository.save(appt(s, a1, "a@x.com", "1 A St",
                LocalDateTime.now().plusDays(5).withHour(12).withMinute(0)));
        Appointment ap2 = appointmentRepository.save(appt(s, a2, "b@x.com", "2 B St",
                LocalDateTime.now().plusDays(6).withHour(14).withMinute(0)));

        receiptRepository.save(rcpt(ap1, "80.00", "CASH"));
        receiptRepository.save(rcpt(ap2, "70.50", "CARD"));

        assertEquals(new BigDecimal("150.50"), receiptRepository.sumTotalRevenue());
    }
}