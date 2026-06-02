package com.example.inventory_service.infra.kafka.producer;

import com.example.inventory_service.infra.kafka.events.StockFailedEvent;
import com.example.inventory_service.infra.kafka.events.StockReservedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class StockEventProducer {
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publishStockReserved(String orderId) {
        var event = new StockReservedEvent(UUID.randomUUID().toString(), orderId, LocalDateTime.now());
        kafkaTemplate.send("stock-reserved", orderId, event);
    }

    public void publishStockFailed(String orderId, String reason) {
        var event = new StockFailedEvent(UUID.randomUUID().toString(), orderId, reason, LocalDateTime.now());
        kafkaTemplate.send("stock-failed", orderId, event);
    }
}
