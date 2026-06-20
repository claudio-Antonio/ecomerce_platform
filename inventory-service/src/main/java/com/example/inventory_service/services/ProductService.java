package com.example.inventory_service.services;

import com.example.inventory_service.domain.Product;
import com.example.inventory_service.dtos.requests.ProductRequest;
import com.example.inventory_service.dtos.responses.ProductResponse;
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

    /**
     * Busca a ENTIDADE gerenciada pelo JPA — sem cache.
     * Uso interno do service para qualquer operação que precise modificar
     * e persistir (update, delete, ajuste de estoque).
     * Nunca expor isso fora do service: Product tem relacionamento
     * bidirecional com StockMovement, e serializar a entidade direto
     * (JSON ou Redis) causa StackOverflowError por ciclo de referência.
     */
    private Product findEntityById(UUID id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));
    }

    /**
     * Versão pública da busca de entidade, exposta apenas para o StockService,
     * que precisa fazer ajustes atômicos de estoque (reservedQuantity/stockQuantity)
     * dentro de uma transação Kafka. Não é cacheada de propósito — estoque
     * é dado de altíssima volatilidade, cachear geraria condição de corrida
     * entre o cache e o banco real.
     */
    public Product findEntityForStockUpdate(UUID id) {
        return findEntityById(id);
    }

    /**
     * Busca o DTO — esse sim é cacheado no Redis.
     * ProductResponse é um record simples, sem proxies Hibernate,
     * sem listas bidirecionais, então serializa sem problemas.
     */
    @Cacheable("products")
    public ProductResponse findById(UUID id) {
        return new ProductResponse(findEntityById(id));
    }

    @Cacheable("product-prices")
    public Double getPrice(UUID id) {
        return findEntityById(id).getPrice();
    }

    /**
     * Persiste mudanças de estoque (reservedQuantity e/ou stockQuantity)
     * feitas pelo StockService. Recebe a própria entidade já modificada
     * em memória e apenas salva — não reconstrói nem recria o produto.
     */
    @CacheEvict(value = {"products", "product-prices"}, key = "#product.id")
    public Product saveStockChange(Product product) {
        return productRepository.save(product);
    }

    @CacheEvict(value = {"products", "product-prices"}, key = "#id")
    public Product update(UUID id, ProductRequest data) {
        Product product = findEntityById(id);
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
        Product product = findEntityById(id);
        productRepository.delete(product);
    }
}