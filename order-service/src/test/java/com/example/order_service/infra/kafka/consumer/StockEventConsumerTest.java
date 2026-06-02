package com.example.order_service.infra.kafka.consumer;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.example.order_service.domain.Order;
import com.example.order_service.domain.enums.OrderStatus;
import com.example.order_service.infra.kafka.events.StockFailedEvent;
import com.example.order_service.infra.kafka.events.StockReservedEvent;
import com.example.order_service.repositories.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@ExtendWith(MockitoExtension.class)
class StockEventConsumerTest {

    @InjectMocks
    private StockEventConsumer stockEventConsumer;

    @Mock
    private OrderRepository orderRepository;

    @Captor
    private ArgumentCaptor<Order> orderCaptor;

    private UUID orderId;
    private Order order;

    @BeforeEach
    void setUp() {
        orderId = UUID.randomUUID();

        order = new Order();
        order.setId(orderId);
        order.setStatus(OrderStatus.PENDING);
        order.setCreatedAt(LocalDateTime.now().minusMinutes(5));
        order.setUpdatedAt(LocalDateTime.now().minusMinutes(5));
    }

    @Test
    @DisplayName("Should update order status to CONFIRMED and save when stock reserved event is received")
    void onStockReserved_shouldConfirmOrderSuccessfully() {
        StockReservedEvent event = new StockReservedEvent(
                UUID.randomUUID().toString(),
                orderId.toString(),
                LocalDateTime.now()
        );

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenReturn(order);

        stockEventConsumer.onStockReserved(event);

        verify(orderRepository, times(1)).findById(orderId);
        verify(orderRepository, times(1)).save(orderCaptor.capture());

        Order updatedOrder = orderCaptor.getValue();
        assertThat(updatedOrder.getStatus()).isEqualTo(OrderStatus.CONFIRMED);
        assertThat(updatedOrder.getUpdatedAt()).isAfter(order.getCreatedAt());
    }

    @Test
    @DisplayName("Should update order status to CANCELLED and save when stock failed event is received")
    void onStockFailed_shouldCancelOrderSuccessfully() {
        StockFailedEvent event = new StockFailedEvent(
                UUID.randomUUID().toString(),
                orderId.toString(),
                "Out of stock",
                LocalDateTime.now()
        );

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenReturn(order);

        stockEventConsumer.onStockFailed(event);

        verify(orderRepository, times(1)).findById(orderId);
        verify(orderRepository, times(1)).save(orderCaptor.capture());

        Order updatedOrder = orderCaptor.getValue();
        assertThat(updatedOrder.getStatus()).isEqualTo(OrderStatus.CANCELLED);
        assertThat(updatedOrder.getUpdatedAt()).isAfter(order.getCreatedAt());
    }

    @Test
    @DisplayName("Should do nothing and never save when stock reserved event references a non-existent order id")
    void onStockReserved_shouldDoNothingWhenOrderNotFound() {
        StockReservedEvent event = new StockReservedEvent(
                UUID.randomUUID().toString(),
                orderId.toString(),
                LocalDateTime.now()
        );

        when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

        stockEventConsumer.onStockReserved(event);

        verify(orderRepository, times(1)).findById(orderId);
        verify(orderRepository, never()).save(any(Order.class));
    }
}