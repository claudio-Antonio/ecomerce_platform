package com.example.order_service.controllers;

import com.example.order_service.domain.Order;
import com.example.order_service.dtos.requests.OrderRequest;
import com.example.order_service.dtos.responses.OrderResponse;
import com.example.order_service.services.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService service;

    @PostMapping
    public ResponseEntity<OrderResponse> create(@RequestBody OrderRequest request) {
        Order saved = service.create(request);

        OrderResponse response = new OrderResponse(
                saved.getId(),
                saved.getUserId(),
                saved.getTotalAmount(),
                saved.getStatus().name(),
                saved.getCreatedAt(),
                null, // itens
                (saved.getPayment() != null) ? saved.getPayment().getId() : null
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<OrderResponse>> findAll() {
        List<OrderResponse> responseList = service.findAll().stream()
                .map(o -> new OrderResponse(
                        o.getId(),
                        o.getUserId(),
                        o.getTotalAmount(),
                        o.getStatus().name(),
                        o.getCreatedAt(),
                        null, // itens
                        (o.getPayment() != null) ? o.getPayment().getId() : null
                ))
                .toList();
        return ResponseEntity.ok(responseList);
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> findById(@PathVariable UUID id) {
        Order o = service.findById(id);

        OrderResponse response = new OrderResponse(
                o.getId(),
                o.getUserId(),
                o.getTotalAmount(),
                o.getStatus().name(),
                o.getCreatedAt(),
                null, // itens
                (o.getPayment() != null) ? o.getPayment().getId() : null
        );

        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<OrderResponse> update(@PathVariable UUID id, @RequestBody OrderRequest request) {
        Order o = service.update(id, request);

        OrderResponse response = new OrderResponse(
                o.getId(),
                o.getUserId(),
                o.getTotalAmount(),
                o.getStatus().name(),
                o.getCreatedAt(),
                null, // itens
                (o.getPayment() != null) ? o.getPayment().getId() : null
        );

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
