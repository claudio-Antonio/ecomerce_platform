package com.example.inventory_service.controllers;

import com.example.inventory_service.domain.Category;
import com.example.inventory_service.dtos.requests.CategoryRequest;
import com.example.inventory_service.dtos.responses.CategoryResponse;
import com.example.inventory_service.services.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping(value = "/api/categories")
public class CategoryController {
    private final CategoryService categoryService;

    @GetMapping
    public ResponseEntity<List<CategoryResponse>> findAll() {
        List<CategoryResponse> responses = categoryService.findAll().stream().map(CategoryResponse::new).toList();
        return ResponseEntity.ok().body(responses);
    }

    @GetMapping(path = "/{id}")
    public ResponseEntity<CategoryResponse> findById(@PathVariable UUID id) {
        CategoryResponse response = new CategoryResponse(categoryService.findById(id));
        return ResponseEntity.ok().body(response);
    }

    @PostMapping
    public ResponseEntity<CategoryResponse> saveCategory(@RequestBody CategoryRequest request) {
        Category newCategory = categoryService.create(request);
        return ResponseEntity.ok().body(new CategoryResponse(newCategory));
    }

    @PutMapping(path = "/{id}")
    public ResponseEntity<CategoryResponse> updateCategory(@PathVariable UUID id, @RequestBody CategoryRequest request) {
        Category updatedCategory =  categoryService.update(id, request);
        return ResponseEntity.ok().body(new CategoryResponse(updatedCategory));
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteCategory(@RequestParam UUID id) {
        categoryService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
