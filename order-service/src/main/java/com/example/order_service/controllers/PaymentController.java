package com.example.order_service.controllers;

import com.example.order_service.domain.Payment;
import com.example.order_service.dtos.requests.PaymentRequest;
import com.example.order_service.dtos.responses.PaymentResponse;
import com.example.order_service.services.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService service;

    // Método auxiliar para converter Entidade -> DTO
    private PaymentResponse mapToResponse(Payment p) {
        return new PaymentResponse(
                p.getId(),
                p.getPaymentMethod(),
                p.getTransactionId(),
                p.getStatus().name(),
                p.getAmount(),
                p.getProcessedAt()
        );
    }

    @PostMapping
    public ResponseEntity<PaymentResponse> create(@RequestBody PaymentRequest request) {
        Payment created = service.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(mapToResponse(created));
    }

    @GetMapping
    public ResponseEntity<List<PaymentResponse>> findAll() {
        List<PaymentResponse> list = service.findAll().stream()
                .map(this::mapToResponse)
                .toList();
        return ResponseEntity.ok(list);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PaymentResponse> findById(@PathVariable UUID id) {
        Payment payment = service.findById(id);
        return ResponseEntity.ok(mapToResponse(payment));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PaymentResponse> update(@PathVariable UUID id, @RequestBody PaymentRequest request) {
        Payment updated = service.update(id, request);
        return ResponseEntity.ok(mapToResponse(updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
