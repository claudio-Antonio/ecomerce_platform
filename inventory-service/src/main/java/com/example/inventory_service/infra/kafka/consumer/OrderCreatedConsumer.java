package com.example.inventory_service.infra.kafka.consumer;

import com.example.inventory_service.infra.kafka.events.OrderCreatedEvent;
import com.example.inventory_service.services.StockService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.KafkaListener;

@Configuration
@RequiredArgsConstructor
public class OrderCreatedConsumer {
    private final StockService  stockService;

    @KafkaListener(topics = "order-created", groupId = "stock-service-group")
    public void listen(OrderCreatedEvent event) {
        try {
            stockService.reserveStock(event);
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }
}
