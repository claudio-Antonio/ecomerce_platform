package com.example.order_service.dtos.responses;
import jakarta.validation.Valid;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record OrderResponse(
         UUID id,
         UUID userId,
         Double totalAmount,
         String status,
         LocalDateTime createdAt,
         List<OrderItemResponse> items,
         UUID paymentId
) {}
