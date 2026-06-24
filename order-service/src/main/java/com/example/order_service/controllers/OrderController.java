package com.example.order_service.controllers;

import com.example.order_service.domain.Order;
import com.example.order_service.dtos.requests.OrderRequest;
import com.example.order_service.dtos.responses.OrderResponse;
import com.example.order_service.services.OrderService;
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
@RequestMapping("/api/orders")
@Tag(name = "orders", description = "controller to create, delete, update and find orders")
public class OrderController {

    private final OrderService service;

    @PostMapping
    @Operation(summary = "Create new order")
    @ApiResponse(responseCode = "201", description = "Create order successfully")
    @ApiResponse(responseCode = "400", description = "BadRequestException, order not created")
    @ApiResponse(responseCode = "500", description = "Server error")
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
    @Operation(summary = "Get all orders")
    @ApiResponse(responseCode = "200", description = "List of orders find successfully")
    @ApiResponse(responseCode = "404", description = "Not found, List of orders doesn't exists")
    @ApiResponse(responseCode = "500", description = "Server error")
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
    @Operation(summary = "Get an specific order by id")
    @ApiResponse(responseCode = "200", description = "Order find successfully")
    @ApiResponse(responseCode = "404", description = "Not found, Order don't exist")
    @ApiResponse(responseCode = "500", description = "Server error")
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
    @Operation(summary = "Update an specific order by id")
    @ApiResponse(responseCode = "200", description = "Order updated successfully")
    @ApiResponse(responseCode = "400", description = "BadRequestException, order not updated")
    @ApiResponse(responseCode = "500", description = "Server error")
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
    @Operation(summary = "Delete order by id")
    @ApiResponse(responseCode = "204", description = "Order deleted successfully")
    @ApiResponse(responseCode = "404", description = "Not found, order not deleted")
    @ApiResponse(responseCode = "500", description = "Server error")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
