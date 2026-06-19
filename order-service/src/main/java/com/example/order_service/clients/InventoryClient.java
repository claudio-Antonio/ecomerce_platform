package com.example.order_service.clients;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

@FeignClient(name = "inventory-service", url = "http://localhost:8083")
public interface InventoryClient {
    @GetMapping("/api/products/{id}/price")
    Double getPrice(@PathVariable UUID productId);
}
