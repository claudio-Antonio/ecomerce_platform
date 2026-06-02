package com.example.inventory_service.infra.kafka.events;

import java.time.LocalDateTime;

public record StockReservedEvent(String eventId, String orderId, LocalDateTime ocurredAt) {
}
