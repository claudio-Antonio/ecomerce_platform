package com.example.inventory_service.dtos.requests;

import com.example.inventory_service.domain.enums.MovementType;

import java.util.UUID;

public record StockUpdateRequest(MovementType type, Integer quantity, String reason, UUID orderId, UUID productId) {
}
