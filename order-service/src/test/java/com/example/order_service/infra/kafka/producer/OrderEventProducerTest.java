package com.example.order_service.infra.kafka.producer;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import com.example.order_service.domain.Order;
import com.example.order_service.domain.OrderItem;
import com.example.order_service.infra.kafka.events.OrderCreatedEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@ExtendWith(MockitoExtension.class)
class OrderEventProducerTest {

    @InjectMocks
    private OrderEventProducer orderEventProducer;

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Captor
    private ArgumentCaptor<OrderCreatedEvent> eventCaptor;

    private UUID orderId;
    private UUID userId;
    private UUID productId;
    private Order order;

    @BeforeEach
    void setUp() {
        orderId = UUID.randomUUID();
        userId = UUID.randomUUID();
        productId = UUID.randomUUID();

        OrderItem item = OrderItem.builder()
                .productId(productId)
                .quantity(5)
                .priceAtPurchase(50.0)
                .build();

        order = new Order();
        order.setId(orderId);
        order.setUserId(userId);
        order.setTotalAmount(250.0);
        order.setItems(new ArrayList<>(List.of(item)));
    }

    @Test
    @DisplayName("Should accurately map order domain properties to Kafka record payload and submit message")
    void publishOrderCreated_shouldMapPayloadAndSendToKafkaSuccessfully() {
        orderEventProducer.publishOrderCreated(order);

        verify(kafkaTemplate, times(1)).send(
                eq("order-created"),
                eq(orderId.toString()),
                eventCaptor.capture()
        );

        OrderCreatedEvent capturedEvent = eventCaptor.getValue();

        assertThat(capturedEvent.eventId()).isNotNull().isNotBlank();
        assertThat(capturedEvent.orderId()).isEqualTo(orderId.toString());
        assertThat(capturedEvent.customerId()).isEqualTo(userId.toString());
        assertThat(capturedEvent.totalAmount()).isEqualTo(250.0);
        assertThat(capturedEvent.occurredAt()).isNotNull();

        assertThat(capturedEvent.items()).hasSize(1);
        OrderCreatedEvent.OrderItemEvent capturedItem = capturedEvent.items().get(0);
        assertThat(capturedItem.productId()).isEqualTo(productId.toString());
        assertThat(capturedItem.quantity()).isEqualTo(5);
    }
}