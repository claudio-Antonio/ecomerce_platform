package com.example.order_service.infra.kafka.producer;

import com.example.order_service.domain.Order;
import com.example.order_service.infra.kafka.events.OrderCreatedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class OrderEventProducer {
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publishOrderCreated(Order order) {
        List<OrderCreatedEvent.OrderItemEvent> items = order.getItems().stream()
                .map(i -> new OrderCreatedEvent.OrderItemEvent(
                        i.getProductId().toString(),
                        i.getQuantity()
                )).toList();

        var event = new OrderCreatedEvent(
                UUID.randomUUID().toString(),
                order.getId().toString(),
                order.getUserId().toString(),
                items,
                order.getTotalAmount(),
                LocalDateTime.now()
        );

        kafkaTemplate.send("order-created", order.getId().toString(), event);
    }
}
