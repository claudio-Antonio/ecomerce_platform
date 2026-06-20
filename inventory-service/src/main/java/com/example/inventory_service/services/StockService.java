package com.example.inventory_service.services;

import com.example.inventory_service.domain.Product;
import com.example.inventory_service.domain.StockMovement;
import com.example.inventory_service.domain.enums.MovementType;
import com.example.inventory_service.infra.kafka.events.OrderCreatedEvent;
import com.example.inventory_service.infra.kafka.producer.StockEventProducer;
import com.example.inventory_service.repositories.StockMovementRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class StockService {
    private final StockMovementRepository stockMovementRepository;
    private final ProductService productService;
    private final StockEventProducer  stockEventProducer;

    @Transactional
    public void reserveStock(OrderCreatedEvent event) {
        List<OrderCreatedEvent.OrderItemEvent> items = event.items();

        for (var item : items) {
            Product product = productService.findEntityForStockUpdate(UUID.fromString(item.productId()));

            int available = product.getStockQuantity() - product.getReservedQuantity();
            if (available < item.quantity()) {
                stockEventProducer.publishStockFailed(event.orderId(), "Estoque insuficiente para o produto: " + product.getName());
                return;
            }
        }

        for (var item : items) {
            Product product = productService.findEntityForStockUpdate(UUID.fromString(item.productId()));
            product.setReservedQuantity(product.getReservedQuantity() + item.quantity());
            productService.saveStockChange(product);

            StockMovement movement = new StockMovement();
            movement.setType(MovementType.RESERVED);
            movement.setQuantity(item.quantity());
            movement.setReason("order-created");
            movement.setOrderId(UUID.fromString(event.orderId()));
            movement.setCreatedAt(LocalDateTime.now());
            movement.setProduct(product);
            stockMovementRepository.save(movement);
        }

        stockEventProducer.publishStockReserved(event.orderId());
    }

    @Transactional
    public void releaseStock(String orderId) {
        List<StockMovement> reservations = stockMovementRepository.findByOrderIdAndType(UUID.fromString(orderId), MovementType.RESERVED);

        for (StockMovement reservation : reservations) {
            Product product = reservation.getProduct();
            product.setReservedQuantity(product.getReservedQuantity() - reservation.getQuantity());
            productService.saveStockChange(product);

            StockMovement release = new StockMovement();
            release.setType(MovementType.RELEASED);
            release.setQuantity(reservation.getQuantity());
            release.setReason("order-cancelled");
            release.setOrderId(UUID.fromString(orderId));
            release.setCreatedAt(LocalDateTime.now());
            release.setProduct(product);
            stockMovementRepository.save(release);
        }
    }

    @Transactional
    public void confirmStock(String orderId) {
        List<StockMovement> reservations = stockMovementRepository.findByOrderIdAndType(UUID.fromString(orderId), MovementType.RESERVED);

        for (StockMovement reservation : reservations) {
            Product product = reservation.getProduct();
            product.setStockQuantity(product.getStockQuantity() - reservation.getQuantity());
            product.setReservedQuantity(product.getReservedQuantity() - reservation.getQuantity());
            productService.saveStockChange(product);

            StockMovement out = new StockMovement();
            out.setType(MovementType.OUT);
            out.setQuantity(reservation.getQuantity());
            out.setReason("order-delivered");
            out.setOrderId(UUID.fromString(orderId));
            out.setCreatedAt(LocalDateTime.now());
            out.setProduct(product);
            stockMovementRepository.save(out);
        }
    }
}