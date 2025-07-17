package com.tanyourpeach.backend.service;

import com.tanyourpeach.backend.model.Appointment;
import com.tanyourpeach.backend.model.Receipt;
import com.tanyourpeach.backend.repository.AppointmentRepository;
import com.tanyourpeach.backend.repository.ReceiptRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
class ReceiptServiceTest {

    @Mock
    private ReceiptRepository receiptRepository;

    @Mock
    private AppointmentRepository appointmentRepository;

    @InjectMocks
    private ReceiptService receiptService;

    private Appointment appointment;
    private Receipt receipt;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        appointment = new Appointment();
        appointment.setAppointmentId(1L);
        appointment.setTotalPrice(120.0);

        receipt = new Receipt();
        receipt.setReceiptId(10L);
        receipt.setAppointment(appointment);
        receipt.setTotalAmount(BigDecimal.valueOf(120.0));
        receipt.setPaymentMethod("Cash");
        receipt.setNotes("Paid in person");
    }

    @Test
    void getAllReceipts_shouldReturnList() {
        when(receiptRepository.findAll()).thenReturn(List.of(receipt));

        List<Receipt> result = receiptService.getAllReceipts();

        assertEquals(1, result.size());
        assertEquals(receipt.getReceiptId(), result.get(0).getReceiptId());
    }

    @Test
    void getReceiptById_shouldReturnReceipt() {
        when(receiptRepository.findById(10L)).thenReturn(Optional.of(receipt));

        Optional<Receipt> result = receiptService.getReceiptById(10L);

        assertTrue(result.isPresent());
        assertEquals("Cash", result.get().getPaymentMethod());
    }

    @Test
    void getReceiptByAppointmentId_shouldReturnReceipt() {
        when(receiptRepository.findByAppointment_AppointmentId(1L)).thenReturn(receipt);

        Optional<Receipt> result = receiptService.getReceiptByAppointmentId(1L);

        assertTrue(result.isPresent());
        assertEquals(receipt.getTotalAmount(), result.get().getTotalAmount());
    }

    @Test
    void getReceiptByAppointmentId_shouldReturnEmpty_whenMissing() {
        when(receiptRepository.findByAppointment_AppointmentId(777L)).thenReturn(null);

        Optional<Receipt> result = receiptService.getReceiptByAppointmentId(777L);

        assertTrue(result.isEmpty());
    }

    @Test
    void createReceipt_shouldLinkAppointmentProperly() {
        Receipt newReceipt = new Receipt();
        newReceipt.setPaymentMethod("Cash");

        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));
        when(receiptRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        Optional<Receipt> result = receiptService.createReceipt(1L, newReceipt);

        assertTrue(result.isPresent());
        assertEquals(1L, result.get().getAppointment().getAppointmentId());
    }

    @Test
    void createReceipt_shouldPreserveProvidedAmount_whenSet() {
        Receipt newReceipt = new Receipt();
        newReceipt.setTotalAmount(BigDecimal.valueOf(88.00));
        newReceipt.setPaymentMethod("Card");

        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));
        when(receiptRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        Optional<Receipt> result = receiptService.createReceipt(1L, newReceipt);

        assertTrue(result.isPresent());
        assertEquals(BigDecimal.valueOf(88.00), result.get().getTotalAmount());
    }

    @Test
    void createReceipt_shouldUseAppointmentTotal_whenNoAmountGiven() {
        Receipt newReceipt = new Receipt();
        newReceipt.setPaymentMethod("Venmo");

        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));
        when(receiptRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        Optional<Receipt> result = receiptService.createReceipt(1L, newReceipt);

        assertTrue(result.isPresent());
        assertEquals(BigDecimal.valueOf(120.0), result.get().getTotalAmount());
        assertEquals("Venmo", result.get().getPaymentMethod());
    }

    @Test
    void createReceipt_shouldAllowNullPaymentMethod() {
        Receipt newReceipt = new Receipt(); // no payment method
        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));
        when(receiptRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        Optional<Receipt> result = receiptService.createReceipt(1L, newReceipt);

        assertTrue(result.isPresent());
        assertNull(result.get().getPaymentMethod());
    }

    @Test
    void createReceipt_shouldReturnEmpty_whenAppointmentMissing() {
        Receipt newReceipt = new Receipt();
        when(appointmentRepository.findById(99L)).thenReturn(Optional.empty());

        Optional<Receipt> result = receiptService.createReceipt(99L, newReceipt);

        assertTrue(result.isEmpty());
        verify(receiptRepository, never()).save(any());
    }

    @Test
    void updateReceipt_shouldApplyChanges() {
        Receipt updated = new Receipt();
        updated.setPaymentMethod("Zelle");
        updated.setNotes("Paid electronically");
        updated.setTotalAmount(BigDecimal.valueOf(150.0));

        when(receiptRepository.findById(10L)).thenReturn(Optional.of(receipt));
        when(receiptRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        Optional<Receipt> result = receiptService.updateReceipt(10L, updated);

        assertTrue(result.isPresent());
        assertEquals("Zelle", result.get().getPaymentMethod());
        assertEquals("Paid electronically", result.get().getNotes());
        assertEquals(BigDecimal.valueOf(150.0), result.get().getTotalAmount());
    }

    @Test
    void updateReceipt_shouldAllowNullNotes() {
        receipt.setNotes("Previous note");
        Receipt updated = new Receipt();
        updated.setPaymentMethod("Venmo");
        updated.setNotes(null); // explicitly null

        when(receiptRepository.findById(10L)).thenReturn(Optional.of(receipt));
        when(receiptRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        Optional<Receipt> result = receiptService.updateReceipt(10L, updated);

        assertTrue(result.isPresent());
        assertNull(result.get().getNotes());
    }

    @Test
    void updateReceipt_shouldReturnEmpty_whenReceiptMissing() {
        Receipt updated = new Receipt();
        updated.setPaymentMethod("CashApp");
        updated.setTotalAmount(BigDecimal.valueOf(50));

        when(receiptRepository.findById(999L)).thenReturn(Optional.empty());

        Optional<Receipt> result = receiptService.updateReceipt(999L, updated);

        assertTrue(result.isEmpty());
        verify(receiptRepository, never()).save(any());
    }

    @Test
    void updateReceipt_shouldNotSetNullTotalAmount() {
        receipt.setTotalAmount(BigDecimal.valueOf(120.0));

        Receipt updated = new Receipt();
        updated.setPaymentMethod("Zelle");
        updated.setTotalAmount(null); // simulate null update

        when(receiptRepository.findById(10L)).thenReturn(Optional.of(receipt));
        when(receiptRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        Optional<Receipt> result = receiptService.updateReceipt(10L, updated);

        assertTrue(result.isPresent());
        assertEquals(BigDecimal.valueOf(120.0), result.get().getTotalAmount()); // unchanged
    }

    @Test
    void deleteReceipt_shouldDeleteWhenExists() {
        when(receiptRepository.existsById(10L)).thenReturn(true);

        boolean result = receiptService.deleteReceipt(10L);

        assertTrue(result);
        verify(receiptRepository).deleteById(10L);
    }

    @Test
    void deleteReceipt_shouldFailWhenMissing() {
        when(receiptRepository.existsById(99L)).thenReturn(false);

        boolean result = receiptService.deleteReceipt(99L);

        assertFalse(result);
        verify(receiptRepository, never()).deleteById(any());
    }
}