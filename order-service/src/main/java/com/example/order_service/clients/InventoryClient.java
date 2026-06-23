package com.example.order_service.clients;

import com.example.order_service.infra.Feign.FeignConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

@FeignClient(name = "inventory-service", url = "http://inventory-service:8083", configuration = FeignConfig.class)
public interface InventoryClient {
    @GetMapping("/api/products/{id}/price")
    Double getPrice(@PathVariable UUID id);
}
