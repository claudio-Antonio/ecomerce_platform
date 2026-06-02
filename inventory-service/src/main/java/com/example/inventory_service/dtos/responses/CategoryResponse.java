package com.example.inventory_service.dtos.responses;

import com.example.inventory_service.domain.Category;

import java.time.LocalDateTime;
import java.util.UUID;

public record CategoryResponse(UUID id, String name, String description, LocalDateTime createdAt) {
    public CategoryResponse(Category category) {
        this(category.getId(), category.getName(), category.getDescription(), category.getCreatedAt());
    }
}
