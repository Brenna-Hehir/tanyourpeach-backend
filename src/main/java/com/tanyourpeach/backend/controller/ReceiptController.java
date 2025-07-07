package com.tanyourpeach.backend.controller;

import com.tanyourpeach.backend.model.Receipt;
import com.tanyourpeach.backend.service.ReceiptService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/receipts")
@CrossOrigin(origins = "*")
public class ReceiptController {

    @Autowired
    private ReceiptService receiptService;

    // GET all receipts
    @GetMapping
    public List<Receipt> getAllReceipts() {
        return receiptService.getAllReceipts();
    }

    // GET receipt by ID
    @GetMapping("/{id}")
    public ResponseEntity<Receipt> getReceiptById(@PathVariable Long id) {
        return receiptService.getReceiptById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // GET receipt by appointment ID
    @GetMapping("/appointment/{appointmentId}")
    public ResponseEntity<Receipt> getReceiptByAppointmentId(@PathVariable Long appointmentId) {
        return receiptService.getReceiptByAppointmentId(appointmentId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // POST create receipt
    @PostMapping("/appointment/{appointmentId}")
    public ResponseEntity<Receipt> createReceipt(@PathVariable Long appointmentId, @RequestBody Receipt receiptData) {
        return receiptService.createReceipt(appointmentId, receiptData)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.badRequest().build());
    }

    // PUT update receipt
    @PutMapping("/{id}")
    public ResponseEntity<Receipt> updateReceipt(@PathVariable Long id, @RequestBody Receipt updated) {
        return receiptService.updateReceipt(id, updated)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // DELETE receipt
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReceipt(@PathVariable Long id) {
        return receiptService.deleteReceipt(id)
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }
}