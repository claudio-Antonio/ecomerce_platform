package com.example.order_service.dtos.responses;

import jakarta.validation.Valid;

import java.util.UUID;

public record OrderItemResponse( UUID productId, Integer quantity, Double priceAtPurchase) {
}
