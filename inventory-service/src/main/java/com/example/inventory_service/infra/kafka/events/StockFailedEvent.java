package com.example.inventory_service.infra.kafka.events;

import java.time.LocalDateTime;

public record StockFailedEvent(String eventId, String orderId, String reason, LocalDateTime ocurredAt) {
}
