package com.example.order_service.infra.kafka.consumer;

import com.example.order_service.domain.enums.OrderStatus;
import com.example.order_service.infra.kafka.events.StockFailedEvent;
import com.example.order_service.infra.kafka.events.StockReservedEvent;
import com.example.order_service.repositories.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class StockEventConsumer {
    private final OrderRepository orderRepository;

    @KafkaListener(topics = "stock-reserved", groupId = "order-service", properties = {"spring.json.value.default.type=com.example.order_service.infra.kafka.events.StockReservedEvent"})
    public void onStockReserved(StockReservedEvent event) {
        orderRepository
                .findById(UUID.fromString(event.orderId()))
                .ifPresent(order -> {
                    order.setStatus(OrderStatus.CONFIRMED);
                    order.setUpdatedAt(LocalDateTime.now());
                    orderRepository.save(order);
                });
    }

    @KafkaListener(topics = "stock-failed", groupId = "order-service", properties = {"spring.json.value.default.type=com.example.order_service.infra.kafka.events.StockFailedEvent"})
    public void onStockFailed(StockFailedEvent event) {
        orderRepository
                .findById(UUID.fromString(event.orderId()))
                .ifPresent(order -> {
                    order.setStatus(OrderStatus.CANCELLED);
                    order.setUpdatedAt(LocalDateTime.now());
                    orderRepository.save(order);
                });
    }
}
