package com.example.order_service.services;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.example.order_service.domain.Order;
import com.example.order_service.domain.OrderItem;
import com.example.order_service.dtos.requests.OrderItemRequest;
import com.example.order_service.infra.redis.ProductPriceCacheService;
import com.example.order_service.repositories.OrderItemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

@ExtendWith(MockitoExtension.class)
class OrderItemServiceTest {

    @InjectMocks
    private OrderItemService orderItemService;

    @Mock
    private OrderItemRepository repository;

    @Mock
    private ProductPriceCacheService productPriceCacheService;

    @Mock
    private OrderService orderService;

    private UUID itemId;
    private UUID productId;
    private UUID orderId;
    private Order order;
    private OrderItem orderItem;
    private OrderItemRequest orderItemRequest;

    @BeforeEach
    void setUp() {
        itemId = UUID.randomUUID();
        productId = UUID.randomUUID();
        orderId = UUID.randomUUID();

        order = new Order();
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
    @DisplayName("Should retrieve price and order, create item and save successfully")
    void create_shouldCreateOrderItemSuccessfully() {
        when(productPriceCacheService.getPrice(productId)).thenReturn(150.0);
        when(orderService.findById(orderId)).thenReturn(order);
        when(repository.save(any(OrderItem.class))).thenReturn(orderItem);

        OrderItem result = orderItemService.create(orderItemRequest);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(itemId);
        assertThat(result.getProductId()).isEqualTo(productId);
        assertThat(result.getPriceAtPurchase()).isEqualTo(150.0);
        assertThat(result.getOrder().getId()).isEqualTo(orderId);

        verify(productPriceCacheService, times(1)).getPrice(productId);
        verify(orderService, times(1)).findById(orderId);
        verify(repository, times(1)).save(any(OrderItem.class));
    }

    @Test
    @DisplayName("Should return order item when searched by valid id")
    void findById_shouldReturnItemWhenFound() {
        when(repository.findById(itemId)).thenReturn(Optional.of(orderItem));

        OrderItem result = orderItemService.findById(itemId);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(itemId);
        verify(repository, times(1)).findById(itemId);
    }

    @Test
    @DisplayName("Should throw RuntimeException when item is not found by id")
    void findById_shouldThrowRuntimeExceptionWhenNotFound() {
        UUID randomId = UUID.randomUUID();
        when(repository.findById(randomId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderItemService.findById(randomId))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Item não encontrado");
        verify(repository, times(1)).findById(randomId);
    }

    @Test
    @DisplayName("Should modify properties and update order item database record successfully")
    void update_shouldUpdateAndReturnItem() {
        UUID newProductId = UUID.randomUUID();
        OrderItemRequest updateRequest = new OrderItemRequest(newProductId, 5, orderId);

        OrderItem updatedItem = OrderItem.builder()
                .id(itemId)
                .productId(newProductId)
                .quantity(5)
                .priceAtPurchase(200.0)
                .order(order)
                .build();

        when(repository.findById(itemId)).thenReturn(Optional.of(orderItem));
        when(productPriceCacheService.getPrice(newProductId)).thenReturn(200.0);
        when(orderService.findById(orderId)).thenReturn(order);
        when(repository.save(any(OrderItem.class))).thenReturn(updatedItem);

        OrderItem result = orderItemService.update(itemId, updateRequest);

        assertThat(result).isNotNull();
        assertThat(result.getProductId()).isEqualTo(newProductId);
        assertThat(result.getQuantity()).isEqualTo(5);
        assertThat(result.getPriceAtPurchase()).isEqualTo(200.0);

        verify(repository, times(1)).findById(itemId);
        verify(productPriceCacheService, times(1)).getPrice(newProductId);
        verify(orderService, times(1)).findById(orderId);
        verify(repository, times(1)).save(any(OrderItem.class));
    }

    @Test
    @DisplayName("Should check existence and delete record from repository successfully")
    void delete_shouldDeleteWhenItemExists() {
        when(repository.existsById(itemId)).thenReturn(true);
        doNothing().when(repository).deleteById(itemId);

        orderItemService.delete(itemId);

        verify(repository, times(1)).existsById(itemId);
        verify(repository, times(1)).deleteById(itemId);
    }

    @Test
    @DisplayName("Should throw exception and never invoke deleteById when item does not exist")
    void delete_shouldThrowExceptionWhenItemDoesNotExist() {
        UUID randomId = UUID.randomUUID();
        when(repository.existsById(randomId)).thenReturn(false);

        assertThatThrownBy(() -> orderItemService.delete(randomId))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Item não encontrado para exclusão");

        verify(repository, times(1)).existsById(randomId);
        verify(repository, never()).deleteById(any(UUID.class));
    }
}
