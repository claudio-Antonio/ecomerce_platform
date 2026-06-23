package com.example.order_service.dtos.requests;

import jakarta.validation.Valid;

import java.util.List;
import java.util.UUID;

public record OrderRequest( UUID userId,  List<OrderItemRequest> items,  String paymentMethod) {
}
