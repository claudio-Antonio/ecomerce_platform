package com.example.order_service.services;

import com.example.order_service.clients.InventoryClient;
import com.example.order_service.domain.Order;
import com.example.order_service.domain.OrderItem;
import com.example.order_service.dtos.requests.OrderItemRequest;
import com.example.order_service.infra.redis.ProductPriceCacheService;
import com.example.order_service.repositories.OrderItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;


import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderItemService {
    private final OrderItemRepository repository;
    private final ProductPriceCacheService productPriceCacheService;
    private final OrderService orderService;

    public OrderItem create(OrderItemRequest data) {
        Double currentPrice = productPriceCacheService.getPrice(data.productId());
        Order order = orderService.findById(data.orderId());

        OrderItem newItem = OrderItem.builder()
                .productId(data.productId())
                .quantity(data.quantity())
                .priceAtPurchase(currentPrice)
                .order(order)
                .build();

        return repository.save(newItem);
    }

    public OrderItem findById(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Item não encontrado"));
    }

    public OrderItem update(UUID id, OrderItemRequest data) {
        OrderItem existing = findById(id);
        Double currentPrice = productPriceCacheService.getPrice(data.productId());

        existing.setQuantity(data.quantity());
        existing.setProductId(data.productId());
        existing.setPriceAtPurchase(currentPrice);
        existing.setOrder(orderService.findById(data.orderId()));

        return repository.save(existing);
    }

    public void delete(UUID id) {
        if (!repository.existsById(id)) {
            throw new RuntimeException("Item não encontrado para exclusão");
        }
        repository.deleteById(id);
    }
}