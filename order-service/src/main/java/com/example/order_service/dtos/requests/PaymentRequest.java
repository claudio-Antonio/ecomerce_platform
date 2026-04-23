package com.example.order_service.dtos.requests;

import jakarta.validation.Valid;

public record PaymentRequest( String paymentMethod) {
}
