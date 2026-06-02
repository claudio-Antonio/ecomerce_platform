package com.example.inventory_service.infra.kafka.events;

import java.time.LocalDateTime;
import java.util.List;

public record OrderCreatedEvent(String eventId, String orderId, String customerId, List<OrderItemEvent> items, Double totalAmount, LocalDateTime ocurredAt) {
    public record OrderItemEvent(String productId, Integer quantity){}
}
