package com.example.order_service.controllers;

import com.example.order_service.domain.Payment;
import com.example.order_service.dtos.requests.PaymentRequest;
import com.example.order_service.dtos.responses.PaymentResponse;
import com.example.order_service.services.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/payments")
@Tag(name = "payments", description = "controller to create, delete, update and find payments")
public class PaymentController {

    private final PaymentService service;

    // Method to convert Entity -> DTO
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
    @Operation(summary = "Create new payment")
    @ApiResponse(responseCode = "201", description = "Create payment successfully")
    @ApiResponse(responseCode = "400", description = "BadRequestException, payment not created")
    @ApiResponse(responseCode = "500", description = "Server error")
    public ResponseEntity<PaymentResponse> create(@RequestBody PaymentRequest request) {
        Payment created = service.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(mapToResponse(created));
    }

    @GetMapping
    @Operation(summary = "Get all payments")
    @ApiResponse(responseCode = "200", description = "List of payments find successfully")
    @ApiResponse(responseCode = "404", description = "Not found, List of payments doesn't exists")
    @ApiResponse(responseCode = "500", description = "Server error")
    public ResponseEntity<List<PaymentResponse>> findAll() {
        List<PaymentResponse> list = service.findAll().stream()
                .map(this::mapToResponse)
                .toList();
        return ResponseEntity.ok(list);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get an specific payment by id")
    @ApiResponse(responseCode = "200", description = "Payment find successfully")
    @ApiResponse(responseCode = "404", description = "Not found, Payment don't exist")
    @ApiResponse(responseCode = "500", description = "Server error")
    public ResponseEntity<PaymentResponse> findById(@PathVariable UUID id) {
        Payment payment = service.findById(id);
        return ResponseEntity.ok(mapToResponse(payment));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an specific payment by id")
    @ApiResponse(responseCode = "200", description = "Payment updated successfully")
    @ApiResponse(responseCode = "400", description = "BadRequestException, payment not updated")
    @ApiResponse(responseCode = "500", description = "Server error")
    public ResponseEntity<PaymentResponse> update(@PathVariable UUID id, @RequestBody PaymentRequest request) {
        Payment updated = service.update(id, request);
        return ResponseEntity.ok(mapToResponse(updated));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete payment by id")
    @ApiResponse(responseCode = "204", description = "Payment deleted successfully")
    @ApiResponse(responseCode = "404", description = "Not found, payment not deleted")
    @ApiResponse(responseCode = "500", description = "Server error")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
