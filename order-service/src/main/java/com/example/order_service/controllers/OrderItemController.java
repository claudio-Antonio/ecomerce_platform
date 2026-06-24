package com.example.order_service.controllers;

import com.example.order_service.domain.OrderItem;
import com.example.order_service.dtos.requests.OrderItemRequest;
import com.example.order_service.dtos.responses.OrderItemResponse;
import com.example.order_service.services.OrderItemService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/items")
@CrossOrigin(origins = "*")
@Tag(name = "order items", description = "controller to create, delete, update and find order items")
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
    @Operation(summary = "Create new order item")
    @ApiResponse(responseCode = "201", description = "Create order item successfully")
    @ApiResponse(responseCode = "400", description = "BadRequestException, order item not created")
    @ApiResponse(responseCode = "500", description = "Server error")
    public ResponseEntity<OrderItemResponse> create(@RequestBody OrderItemRequest request) {
        OrderItem saved = service.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(mapToResponse(saved));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get an specific order item by id")
    @ApiResponse(responseCode = "200", description = "Order item find successfully")
    @ApiResponse(responseCode = "404", description = "Not found, Order item don't exist")
    @ApiResponse(responseCode = "500", description = "Server error")
    public ResponseEntity<OrderItemResponse> findById(@PathVariable UUID id) {
        OrderItem item = service.findById(id);
        return ResponseEntity.ok(mapToResponse(item));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an specific order item by id")
    @ApiResponse(responseCode = "200", description = "Order item updated successfully")
    @ApiResponse(responseCode = "400", description = "BadRequestException, order item not updated")
    @ApiResponse(responseCode = "500", description = "Server error")
    public ResponseEntity<OrderItemResponse> update(@PathVariable UUID id, @RequestBody OrderItemRequest data) {
        OrderItem updatedItem = service.update(id, data);
        return ResponseEntity.ok(mapToResponse(updatedItem));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete order item by id")
    @ApiResponse(responseCode = "204", description = "Order item deleted successfully")
    @ApiResponse(responseCode = "404", description = "Not found, order item not deleted")
    @ApiResponse(responseCode = "500", description = "Server error")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
