package com.example.order_service.dtos.responses;

import java.time.LocalDateTime;
import java.util.UUID;

public record PaymentResponse(
         UUID id,
         String paymentMethod,
         String transactionId,
         String status,
         Double amount,
         LocalDateTime processedAt
) {}
