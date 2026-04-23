package com.example.order_service.controllers;

import com.example.order_service.domain.OrderItem;
import com.example.order_service.dtos.requests.OrderItemRequest;
import com.example.order_service.dtos.responses.OrderItemResponse;
import com.example.order_service.services.OrderItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/items")
public class OrderItemController {

    private final OrderItemService service;

    // Método auxiliar para converter Entidade -> DTO
    private OrderItemResponse mapToResponse(OrderItem item) {
        return new OrderItemResponse(
                item.getProductId(),
                item.getQuantity(),
                item.getPriceAtPurchase()
        );
    }

    @PostMapping
    public ResponseEntity<OrderItemResponse> create(@RequestBody OrderItemRequest request) {
        OrderItem saved = service.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(mapToResponse(saved));
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderItemResponse> findById(@PathVariable UUID id) {
        OrderItem item = service.findById(id);
        return ResponseEntity.ok(mapToResponse(item));
    }

    @PutMapping("/{id}")
    public ResponseEntity<OrderItemResponse> update(@PathVariable UUID id, @RequestBody OrderItemRequest data) {
        OrderItem updatedItem = service.update(id, data);
        return ResponseEntity.ok(mapToResponse(updatedItem));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
