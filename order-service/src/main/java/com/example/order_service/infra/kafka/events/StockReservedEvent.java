package com.example.order_service.infra.kafka.events;

import java.time.LocalDateTime;

public record StockReservedEvent(String eventId, String orderId, LocalDateTime occurredAt) {
}
