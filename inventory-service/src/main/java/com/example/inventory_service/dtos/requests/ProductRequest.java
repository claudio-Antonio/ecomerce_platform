package com.example.inventory_service.dtos.requests;

import java.util.UUID;

public record ProductRequest(String name, String description, Double price, Integer stockQuantity, String sku, Boolean active, UUID categoryId, String imageUrl) {
}
