package com.example.inventory_service.dtos.responses;

import com.example.inventory_service.domain.Product;

import java.time.LocalDateTime;
import java.util.UUID;

public record ProductResponse(
        UUID id,
        String name,
        String description,
        Double price,
        String sku,
        Integer availableQuantity, // Cálculo essencial: stock - reserved
        String categoryName,       // Nome para exibição direta
        Boolean active,
        LocalDateTime updatedAt,   // Mais útil que o createdAt para saber se a info é recente
        String imageUrl
) {
    public ProductResponse(Product product) {
        this(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getSku(),
                (product.getStockQuantity() - product.getReservedQuantity()),
                product.getCategory() != null ? product.getCategory().getName() : null,
                product.getActive(),
                product.getUpdatedAt(),
                product.getImageUrl()
        );
    }
}
