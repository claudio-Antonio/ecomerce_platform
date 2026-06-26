package com.example.inventory_service.controllers;

import com.example.inventory_service.domain.Product;
import com.example.inventory_service.dtos.requests.ProductRequest;
import com.example.inventory_service.dtos.responses.ProductResponse;
import com.example.inventory_service.services.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping(value = "/api/products")
@Tag(name = "products", description = "controller to create, delete, update and find products")
public class ProductController {
    private final ProductService  productService;

    @GetMapping
    @Operation(summary = "Get all products")
    @ApiResponse(responseCode = "200", description = "List of products find successfully")
    @ApiResponse(responseCode = "404", description = "Not found, List of products doesn't exists")
    @ApiResponse(responseCode = "500", description = "Server error")
    public ResponseEntity<List<ProductResponse>> listAll() {
        List<ProductResponse> responses = productService.findAll().stream().map(ProductResponse::new).toList();
        return ResponseEntity.ok().body(responses);
    }

    @GetMapping(path = "/{id}")
    @Operation(summary = "Get an specific product by id")
    @ApiResponse(responseCode = "200", description = "Product find successfully")
    @ApiResponse(responseCode = "404", description = "Not found, Product don't exist")
    @ApiResponse(responseCode = "500", description = "Server error")
    public ResponseEntity<ProductResponse> findById(@PathVariable UUID id) {
        return ResponseEntity.ok().body(productService.findById(id));
    }

    @GetMapping(path = "/{id}/price")
    @Operation(summary = "Get the price of a specific product by id")
    @ApiResponse(responseCode = "200", description = "Product price find successfully")
    @ApiResponse(responseCode = "404", description = "Not found, Product don't exist")
    @ApiResponse(responseCode = "500", description = "Server error")
    public ResponseEntity<Double> getPrice(@PathVariable UUID id) {
        return ResponseEntity.ok().body(productService.getPrice(id));
    }

    @PostMapping
    @Operation(summary = "Create new product")
    @ApiResponse(responseCode = "200", description = "Create product successfully")
    @ApiResponse(responseCode = "400", description = "BadRequestException, product not created")
    @ApiResponse(responseCode = "500", description = "Server error")
    public ResponseEntity<ProductResponse> createProduct(@RequestBody ProductRequest request) {
        Product newProduct = productService.create(request);
        return ResponseEntity.ok().body(new ProductResponse(newProduct));
    }

    @PutMapping(path = "/{id}")
    @Operation(summary = "Update an specific product by id")
    @ApiResponse(responseCode = "200", description = "Product updated successfully")
    @ApiResponse(responseCode = "400", description = "BadRequestException, product not updated")
    @ApiResponse(responseCode = "500", description = "Server error")
    public ResponseEntity<ProductResponse> updateProduct(@PathVariable UUID id, @RequestBody ProductRequest request) {
        Product updatedProduct = productService.update(id, request);
        return ResponseEntity.ok().body(new ProductResponse(updatedProduct));
    }

    @DeleteMapping(path = "/{id}")
    @Operation(summary = "Delete product by id")
    @ApiResponse(responseCode = "204", description = "Product deleted successfully")
    @ApiResponse(responseCode = "404", description = "Not found, product not deleted")
    @ApiResponse(responseCode = "500", description = "Server error")
    public ResponseEntity<Void> deleteProduct(@PathVariable UUID id) {
        productService.delete(id);
        return ResponseEntity.noContent().build();
    }
}