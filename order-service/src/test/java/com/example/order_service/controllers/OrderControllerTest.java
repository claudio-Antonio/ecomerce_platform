package com.example.order_service.controllers;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import com.example.order_service.domain.Order;
import com.example.order_service.domain.Payment;
import com.example.order_service.domain.enums.OrderStatus;
import com.example.order_service.dtos.requests.OrderItemRequest;
import com.example.order_service.dtos.requests.OrderRequest;
import com.example.order_service.dtos.responses.OrderResponse;
import com.example.order_service.services.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@ExtendWith(MockitoExtension.class)
class OrderControllerTest {

    @InjectMocks
    private OrderController orderController;

    @Mock
    private OrderService orderService;

    private UUID orderId;
    private UUID userId;
    private UUID paymentId;
    private Order order;
    private OrderRequest orderRequest;

    @BeforeEach
    void setUp() {
        orderId = UUID.randomUUID();
        userId = UUID.randomUUID();
        paymentId = UUID.randomUUID();

        Payment payment = new Payment();
        payment.setId(paymentId);

        order = new Order();
        order.setId(orderId);
        order.setUserId(userId);
        order.setTotalAmount(350.0);
        order.setStatus(OrderStatus.PENDING);
        order.setCreatedAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());
        order.setPayment(payment);
        order.setItems(new ArrayList<>());

        OrderItemRequest itemRequest = new OrderItemRequest(UUID.randomUUID(), 2, null);
        orderRequest = new OrderRequest(userId, List.of(itemRequest), "CREDIT_CARD");
    }

    @Test
    @DisplayName("Should return 201 Created and mapped response payload when creating order")
    void create_shouldReturn201CreatedWithOrderResponse() {
        when(orderService.create(any(OrderRequest.class))).thenReturn(order);

        ResponseEntity<OrderResponse> response = orderController.create(orderRequest);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().id()).isEqualTo(orderId);
        assertThat(response.getBody().userId()).isEqualTo(userId);
        assertThat(response.getBody().status()).isEqualTo("PENDING");
        assertThat(response.getBody().paymentId()).isEqualTo(paymentId);
        verify(orderService, times(1)).create(any(OrderRequest.class));
    }

    @Test
    @DisplayName("Should return 201 Created with null payment id when order has no payment linked")
    void create_shouldReturn201WithNullPaymentIdWhenNoPayment() {
        order.setPayment(null);
        when(orderService.create(any(OrderRequest.class))).thenReturn(order);

        ResponseEntity<OrderResponse> response = orderController.create(orderRequest);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().paymentId()).isNull();
    }

    @Test
    @DisplayName("Should return 200 OK with list of mapped orders when data exists")
    void findAll_shouldReturn200WithListOfOrders() {
        when(orderService.findAll()).thenReturn(List.of(order));

        ResponseEntity<List<OrderResponse>> response = orderController.findAll();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(1);
        assertThat(response.getBody().get(0).id()).isEqualTo(orderId);
        verify(orderService, times(1)).findAll();
    }

    @Test
    @DisplayName("Should return 200 OK with empty list when no orders are present")
    void findAll_shouldReturn200WithEmptyList() {
        when(orderService.findAll()).thenReturn(List.of());

        ResponseEntity<List<OrderResponse>> response = orderController.findAll();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEmpty();
    }

    @Test
    @DisplayName("Should return 200 OK and mapped record when order is located by id")
    void findById_shouldReturn200WithOrder() {
        when(orderService.findById(orderId)).thenReturn(order);

        ResponseEntity<OrderResponse> response = orderController.findById(orderId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().id()).isEqualTo(orderId);
        verify(orderService, times(1)).findById(orderId);
    }

    @Test
    @DisplayName("Should bubble up RuntimeException when fetching non-existent resource")
    void findById_shouldThrowExceptionWhenNotFound() {
        UUID randomId = UUID.randomUUID();
        when(orderService.findById(randomId)).thenThrow(new RuntimeException("Pedido não encontrado"));

        assertThatThrownBy(() -> orderController.findById(randomId))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Pedido não encontrado");
    }

    @Test
    @DisplayName("Should return 200 OK and updated mapped record when order modification succeeds")
    void update_shouldReturn200WithUpdatedOrder() {
        when(orderService.update(eq(orderId), any(OrderRequest.class))).thenReturn(order);

        ResponseEntity<OrderResponse> response = orderController.update(orderId, orderRequest);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().id()).isEqualTo(orderId);
        verify(orderService, times(1)).update(eq(orderId), any(OrderRequest.class));
    }

    @Test
    @DisplayName("Should return 204 No Content when delete invocation completes successfully")
    void delete_shouldReturn204NoContent() {
        doNothing().when(orderService).delete(orderId);

        ResponseEntity<Void> response = orderController.delete(orderId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(orderService, times(1)).delete(orderId);
    }
}