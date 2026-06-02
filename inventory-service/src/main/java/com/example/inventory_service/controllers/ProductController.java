package com.example.inventory_service.controllers;

import com.example.inventory_service.domain.Product;
import com.example.inventory_service.dtos.requests.ProductRequest;
import com.example.inventory_service.dtos.responses.ProductResponse;
import com.example.inventory_service.services.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping(value = "/api/products")
public class ProductController {
    private final ProductService  productService;

    @GetMapping
    public ResponseEntity<List<ProductResponse>> listAll() {
        List<ProductResponse> responses = productService.findAll().stream().map(ProductResponse::new).toList();
        return ResponseEntity.ok().body(responses);
    }

    @GetMapping(path = "/{id}")
    public ResponseEntity<ProductResponse> findById(@PathVariable UUID id) {
        ProductResponse response = new ProductResponse(productService.findById(id));
        return ResponseEntity.ok().body(response);
    }

    @PostMapping
    public ResponseEntity<ProductResponse> createProduct(@RequestBody ProductRequest request) {
        Product newProduct = productService.create(request);
        return ResponseEntity.ok().body(new ProductResponse(newProduct));
    }

    @PutMapping(path = "/{id}")
    public ResponseEntity<ProductResponse> updateProduct(@PathVariable UUID id, @RequestBody ProductRequest request) {
        Product updatedProduct = productService.update(id, request);
        return ResponseEntity.ok().body(new ProductResponse(updatedProduct));
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteProduct(@RequestParam UUID id) {
        productService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
