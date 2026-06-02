package com.example.order_service.services;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.example.order_service.domain.Order;
import com.example.order_service.domain.OrderItem;
import com.example.order_service.domain.Payment;
import com.example.order_service.domain.enums.OrderStatus;
import com.example.order_service.dtos.requests.OrderItemRequest;
import com.example.order_service.dtos.requests.OrderRequest;
import com.example.order_service.dtos.requests.PaymentRequest;
import com.example.order_service.infra.kafka.producer.OrderEventProducer;
import com.example.order_service.infra.redis.ProductPriceCacheService;
import com.example.order_service.repositories.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @InjectMocks
    private OrderService orderService;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private PaymentService paymentService;

    @Mock
    private OrderEventProducer orderEventProducer;

    @Mock
    private ProductPriceCacheService productPriceCacheService;

    private UUID orderId;
    private UUID userId;
    private UUID productId;
    private Payment payment;
    private Order order;
    private OrderRequest orderRequest;

    @BeforeEach
    void setUp() {
        orderId = UUID.randomUUID();
        userId = UUID.randomUUID();
        productId = UUID.randomUUID();

        payment = new Payment();
        payment.setId(UUID.randomUUID());
        payment.setPaymentMethod("CREDIT_CARD");
        payment.setAmount(200.0);

        OrderItem orderItem = OrderItem.builder()
                .productId(productId)
                .quantity(2)
                .priceAtPurchase(100.0)
                .build();

        order = new Order();
        order.setId(orderId);
        order.setUserId(userId);
        order.setTotalAmount(200.0);
        order.setStatus(OrderStatus.PENDING);
        order.setPayment(payment);
        order.setItems(new ArrayList<>(List.of(orderItem)));
        order.setCreatedAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());

        orderItem.setOrder(order);

        OrderItemRequest itemRequest = new OrderItemRequest(productId, 2, null);
        orderRequest = new OrderRequest(userId, List.of(itemRequest), "CREDIT_CARD");
    }

    @Test
    @DisplayName("Should create order, calculate total amount, save and publish kafka event successfully")
    void create_shouldCreateOrderSuccessfully() {
        when(productPriceCacheService.getPrice(productId)).thenReturn(100.0);
        when(paymentService.create(any(PaymentRequest.class))).thenReturn(payment);
        when(orderRepository.save(any(Order.class))).thenReturn(order);
        doNothing().when(orderEventProducer).publishOrderCreated(any(Order.class));

        Order result = orderService.create(orderRequest);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(orderId);
        assertThat(result.getTotalAmount()).isEqualTo(200.0);
        assertThat(result.getStatus()).isEqualTo(OrderStatus.PENDING);

        verify(productPriceCacheService, times(1)).getPrice(productId);
        verify(paymentService, times(1)).create(any(PaymentRequest.class));
        verify(orderRepository, times(1)).save(any(Order.class));
        verify(orderEventProducer, times(1)).publishOrderCreated(any(Order.class));
    }

    @Test
    @DisplayName("Should return list containing orders when records exist")
    void findAll_shouldReturnListOfOrders() {
        when(orderRepository.findAll()).thenReturn(List.of(order));

        List<Order> result = orderService.findAll();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getUserId()).isEqualTo(userId);
        verify(orderRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Should return empty list when no orders are registered")
    void findAll_shouldReturnEmptyListWhenNoOrders() {
        when(orderRepository.findAll()).thenReturn(List.of());

        List<Order> result = orderService.findAll();

        assertThat(result).isEmpty();
        verify(orderRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Should return order when found by id")
    void findById_shouldReturnOrderWhenFound() {
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        Order result = orderService.findById(orderId);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(orderId);
        verify(orderRepository, times(1)).findById(orderId);
    }

    @Test
    @DisplayName("Should throw RuntimeException when order not found by id")
    void findById_shouldThrowRuntimeExceptionWhenNotFound() {
        UUID randomId = UUID.randomUUID();
        when(orderRepository.findById(randomId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.findById(randomId))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Pedido não encontrado");
        verify(orderRepository, times(1)).findById(randomId);
    }

    @Test
    @DisplayName("Should update user id and update timestamp when modification is valid")
    void update_shouldUpdateAndReturnOrder() {
        UUID newUserId = UUID.randomUUID();
        OrderRequest updateRequest = new OrderRequest(newUserId, List.of(), "CREDIT_CARD");

        Order updatedOrder = new Order();
        updatedOrder.setId(orderId);
        updatedOrder.setUserId(newUserId);
        updatedOrder.setTotalAmount(200.0);
        updatedOrder.setStatus(OrderStatus.PENDING);

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenReturn(updatedOrder);

        Order result = orderService.update(orderId, updateRequest);

        assertThat(result).isNotNull();
        assertThat(result.getUserId()).isEqualTo(newUserId);
        verify(orderRepository, times(1)).findById(orderId);
        verify(orderRepository, times(1)).save(any(Order.class));
    }

    @Test
    @DisplayName("Should throw exception and never save when updating non-existent order")
    void update_shouldThrowExceptionAndNeverSaveWhenOrderNotFound() {
        UUID randomId = UUID.randomUUID();
        when(orderRepository.findById(randomId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.update(randomId, orderRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Pedido não encontrado");

        verify(orderRepository, times(1)).findById(randomId);
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    @DisplayName("Should call repository deleteById when invoking delete method")
    void delete_shouldCallRepositoryDelete() {
        doNothing().when(orderRepository).deleteById(orderId);

        orderService.delete(orderId);

        verify(orderRepository, times(1)).deleteById(orderId);
    }
}