package com.example.inventory_service.services;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.example.inventory_service.domain.Category;
import com.example.inventory_service.domain.Product;
import com.example.inventory_service.domain.StockMovement;
import com.example.inventory_service.domain.enums.MovementType;
import com.example.inventory_service.dtos.requests.ProductRequest;
import com.example.inventory_service.infra.kafka.events.OrderCreatedEvent;
import com.example.inventory_service.infra.kafka.producer.StockEventProducer;
import com.example.inventory_service.repositories.StockMovementRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

@ExtendWith(MockitoExtension.class)
class StockServiceTest {

    @InjectMocks
    private StockService stockService;

    @Mock
    private StockMovementRepository stockMovementRepository;

    @Mock
    private ProductService productService;

    @Mock
    private StockEventProducer stockEventProducer;

    private UUID productId;
    private String orderId;
    private Product product;
    private OrderCreatedEvent orderCreatedEvent;

    @BeforeEach
    void setUp() {
        productId = UUID.randomUUID();
        orderId = UUID.randomUUID().toString();

        Category category = Category.builder().id(UUID.randomUUID()).name("Tech").build();

        product = Product.builder()
                .id(productId)
                .name("Mouse Gamer")
                .price(200.0)
                .stockQuantity(10)
                .reservedQuantity(2)
                .sku("MSE-01")
                .active(true)
                .category(category)
                .build();

        OrderCreatedEvent.OrderItemEvent itemEvent = new OrderCreatedEvent.OrderItemEvent(productId.toString(), 3);
        orderCreatedEvent = new OrderCreatedEvent("evt-123", orderId, "cust-123", List.of(itemEvent), 600.0, null);
    }

    @Test
    @DisplayName("Should reserve stock and publish success event when inventory is available")
    void reserveStock_shouldReserveAndPublishSuccess() {
        when(productService.findById(productId)).thenReturn(product);
        when(productService.create(any(ProductRequest.class))).thenReturn(product);
        when(stockMovementRepository.save(any(StockMovement.class))).thenAnswer(invocation -> invocation.getArgument(0));

        stockService.reserveStock(orderCreatedEvent);

        assertThat(product.getReservedQuantity()).isEqualTo(5);
        verify(stockEventProducer, times(1)).publishStockReserved(orderId);
        verify(stockEventProducer, never()).publishStockFailed(any(), any());
        verify(stockMovementRepository, times(1)).save(any(StockMovement.class));
    }

    @Test
    @DisplayName("Should publish failed event and abort when inventory is insufficient")
    void reserveStock_shouldPublishFailedWhenStockIsInsufficient() {
        OrderCreatedEvent.OrderItemEvent heavyItem = new OrderCreatedEvent.OrderItemEvent(productId.toString(), 20);
        OrderCreatedEvent tightEvent = new OrderCreatedEvent("evt-123", orderId, "cust-123", List.of(heavyItem), 4000.0, null);

        when(productService.findById(productId)).thenReturn(product);

        stockService.reserveStock(tightEvent);

        assertThat(product.getReservedQuantity()).isEqualTo(2); // Não altera
        verify(stockEventProducer, times(1)).publishStockFailed(eq(orderId), anyString());
        verify(stockEventProducer, never()).publishStockReserved(any());
        verify(stockMovementRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should release reserved stock when order is cancelled")
    void releaseStock_shouldDecreaseReservedQuantityAndSaveMovement() {
        StockMovement reservation = StockMovement.builder()
                .product(product)
                .quantity(2)
                .type(MovementType.RESERVED)
                .build();

        when(stockMovementRepository.findByOrderIdAndType(UUID.fromString(orderId), MovementType.RESERVED))
                .thenReturn(List.of(reservation));
        when(productService.create(any(ProductRequest.class))).thenReturn(product);

        stockService.releaseStock(orderId);

        assertThat(product.getReservedQuantity()).isEqualTo(0);
        verify(stockMovementRepository, times(1)).save(any(StockMovement.class));
    }

    @Test
    @DisplayName("Should confirm stock deduction when order delivery is confirmed")
    void confirmStock_shouldDeductStockAndReservationQuantities() {
        StockMovement reservation = StockMovement.builder()
                .product(product)
                .quantity(2)
                .type(MovementType.RESERVED)
                .build();

        when(stockMovementRepository.findByOrderIdAndType(UUID.fromString(orderId), MovementType.RESERVED))
                .thenReturn(List.of(reservation));
        when(productService.create(any(ProductRequest.class))).thenReturn(product);

        stockService.confirmStock(orderId);

        assertThat(product.getStockQuantity()).isEqualTo(8);
        assertThat(product.getReservedQuantity()).isEqualTo(0);
        verify(stockMovementRepository, times(1)).save(any(StockMovement.class));
    }
}
