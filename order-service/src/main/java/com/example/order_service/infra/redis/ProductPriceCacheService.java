package com.example.order_service.infra.redis;

import com.example.order_service.clients.InventoryClient;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProductPriceCacheService {

    private final InventoryClient inventoryClient;

    @Cacheable(value = "product-prices", key = "#productId", unless = "#result == null")
    public Double getPrice(UUID productId) {
        return inventoryClient.getPrice(productId);
    }

    @CacheEvict(value = "product-prices", key = "#productId")
    public void evictPrice(UUID productId) {}
}
