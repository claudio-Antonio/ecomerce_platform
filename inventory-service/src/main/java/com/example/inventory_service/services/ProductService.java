package com.example.inventory_service.services;

import com.example.inventory_service.domain.Product;
import com.example.inventory_service.dtos.requests.ProductRequest;
import com.example.inventory_service.repositories.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class ProductService {
    private final ProductRepository productRepository;
    private final CategoryService categoryService;

    public Product create(ProductRequest data) {
        Product newProduct = Product.builder()
                .name(data.name())
                .description(data.description())
                .price(data.price())
                .stockQuantity(data.stockQuantity())
                .sku(data.sku())
                .active(data.active())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .category(categoryService.findById(data.categoryId()))
                .reservedQuantity(0)
                .imageUrl(data.imageUrl())
                .build();
        return productRepository.save(newProduct);
    }

    public List<Product> findAll() {
        return productRepository.findAll();
    }

    @Cacheable("products")
    public Product findById(UUID id) {
        return productRepository.findById(id).orElseThrow(() -> new RuntimeException("Product not found"));
    }

    @Cacheable("product-prices")
    public Double getPrice(UUID id) {
        Product product = findById(id);
        return product.getPrice();
    }

    @CacheEvict(value = {"products", "product-prices"}, key = "#id")
    public Product update(UUID id, ProductRequest data) {
        Product product = findById(id);
        product.setName(data.name());
        product.setDescription(data.description());
        product.setPrice(data.price());
        product.setStockQuantity(data.stockQuantity());
        product.setSku(data.sku());
        product.setActive(data.active());
        product.setCategory(categoryService.findById(data.categoryId()));
        product.setUpdatedAt(LocalDateTime.now());
        product.setImageUrl(data.imageUrl());
        return productRepository.save(product);
    }

    @CacheEvict(value = {"products", "product-prices"}, key = "#id")
    public void delete(UUID id) {
        Product product = findById(id);
        productRepository.delete(product);
    }
}
