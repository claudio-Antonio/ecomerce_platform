package com.example.order_service.controllers;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import com.example.order_service.domain.Order;
import com.example.order_service.domain.OrderItem;
import com.example.order_service.dtos.requests.OrderItemRequest;
import com.example.order_service.dtos.responses.OrderItemResponse;
import com.example.order_service.services.OrderItemService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.UUID;

@ExtendWith(MockitoExtension.class)
class OrderItemControllerTest {

    @InjectMocks
    private OrderItemController orderItemController;

    @Mock
    private OrderItemService orderItemService;

    private UUID itemId;
    private UUID productId;
    private UUID orderId;
    private OrderItem orderItem;
    private OrderItemRequest orderItemRequest;

    @BeforeEach
    void setUp() {
        itemId = UUID.randomUUID();
        productId = UUID.randomUUID();
        orderId = UUID.randomUUID();

        Order order = new Order();
        order.setId(orderId);

        orderItem = OrderItem.builder()
                .id(itemId)
                .productId(productId)
                .quantity(3)
                .priceAtPurchase(150.0)
                .order(order)
                .build();

        orderItemRequest = new OrderItemRequest(productId, 3, orderId);
    }

    @Test
    @DisplayName("Should return 201 Created and mapped response payload when creating order item")
    void create_shouldReturn201CreatedWithResponse() {
        when(orderItemService.create(any(OrderItemRequest.class))).thenReturn(orderItem);

        ResponseEntity<OrderItemResponse> response = orderItemController.create(orderItemRequest);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().productId()).isEqualTo(productId);
        assertThat(response.getBody().quantity()).isEqualTo(3);
        assertThat(response.getBody().priceAtPurchase()).isEqualTo(150.0);
        verify(orderItemService, times(1)).create(any(OrderItemRequest.class));
    }

    @Test
    @DisplayName("Should return 200 OK and mapped payload when order item is found by id")
    void findById_shouldReturn200WithItem() {
        when(orderItemService.findById(itemId)).thenReturn(orderItem);

        ResponseEntity<OrderItemResponse> response = orderItemController.findById(itemId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().productId()).isEqualTo(productId);
        verify(orderItemService, times(1)).findById(itemId);
    }

    @Test
    @DisplayName("Should bubble up RuntimeException when resource is not found by id")
    void findById_shouldThrowExceptionWhenNotFound() {
        UUID randomId = UUID.randomUUID();
        when(orderItemService.findById(randomId)).thenThrow(new RuntimeException("Item não encontrado"));

        assertThatThrownBy(() -> orderItemController.findById(randomId))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Item não encontrado");
    }

    @Test
    @DisplayName("Should return 200 OK and updated mapped record when order item update succeeds")
    void update_shouldReturn200WithUpdatedItem() {
        when(orderItemService.update(eq(itemId), any(OrderItemRequest.class))).thenReturn(orderItem);

        ResponseEntity<OrderItemResponse> response = orderItemController.update(itemId, orderItemRequest);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().productId()).isEqualTo(productId);
        verify(orderItemService, times(1)).update(eq(itemId), any(OrderItemRequest.class));
    }

    @Test
    @DisplayName("Should return 204 No Content when delete invocation completes successfully")
    void delete_shouldReturn204NoContent() {
        doNothing().when(orderItemService).delete(itemId);

        ResponseEntity<Void> response = orderItemController.delete(itemId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(orderItemService, times(1)).delete(itemId);
    }
}