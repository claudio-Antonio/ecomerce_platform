package com.example.order_service.dtos.requests;

import jakarta.validation.Valid;

import java.util.UUID;

public record OrderItemRequest( UUID productId,  Integer quantity,  UUID orderId) {
}
