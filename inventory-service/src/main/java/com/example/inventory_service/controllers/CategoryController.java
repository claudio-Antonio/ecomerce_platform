package com.example.inventory_service.controllers;

import com.example.inventory_service.domain.Category;
import com.example.inventory_service.dtos.requests.CategoryRequest;
import com.example.inventory_service.dtos.responses.CategoryResponse;
import com.example.inventory_service.services.CategoryService;
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
@RequestMapping(value = "/api/categories")
@Tag(name = "categories", description = "controller to create, delete, update and find categories")
public class CategoryController {
    private final CategoryService categoryService;

    @GetMapping
    @Operation(summary = "Get all categories")
    @ApiResponse(responseCode = "200", description = "List of categories find successfully")
    @ApiResponse(responseCode = "404", description = "Not found, List of categories doesn't exists")
    @ApiResponse(responseCode = "500", description = "Server error")
    public ResponseEntity<List<CategoryResponse>> findAll() {
        List<CategoryResponse> responses = categoryService.findAll().stream().map(CategoryResponse::new).toList();
        return ResponseEntity.ok().body(responses);
    }

    @GetMapping(path = "/{id}")
    @Operation(summary = "Get an specific category by id")
    @ApiResponse(responseCode = "200", description = "Category find successfully")
    @ApiResponse(responseCode = "404", description = "Not found, Category don't exist")
    @ApiResponse(responseCode = "500", description = "Server error")
    public ResponseEntity<CategoryResponse> findById(@PathVariable UUID id) {
        CategoryResponse response = new CategoryResponse(categoryService.findById(id));
        return ResponseEntity.ok().body(response);
    }

    @PostMapping
    @Operation(summary = "Create new category")
    @ApiResponse(responseCode = "200", description = "Create category successfully")
    @ApiResponse(responseCode = "400", description = "BadRequestException, category not created")
    @ApiResponse(responseCode = "500", description = "Server error")
    public ResponseEntity<CategoryResponse> saveCategory(@RequestBody CategoryRequest request) {
        Category newCategory = categoryService.create(request);
        return ResponseEntity.ok().body(new CategoryResponse(newCategory));
    }

    @PutMapping(path = "/{id}")
    @Operation(summary = "Update an specific category by id")
    @ApiResponse(responseCode = "200", description = "Category updated successfully")
    @ApiResponse(responseCode = "400", description = "BadRequestException, category not updated")
    @ApiResponse(responseCode = "500", description = "Server error")
    public ResponseEntity<CategoryResponse> updateCategory(@PathVariable UUID id, @RequestBody CategoryRequest request) {
        Category updatedCategory =  categoryService.update(id, request);
        return ResponseEntity.ok().body(new CategoryResponse(updatedCategory));
    }

    @DeleteMapping
    @Operation(summary = "Delete category by id")
    @ApiResponse(responseCode = "204", description = "Category deleted successfully")
    @ApiResponse(responseCode = "404", description = "Not found, category not deleted")
    @ApiResponse(responseCode = "500", description = "Server error")
    public ResponseEntity<Void> deleteCategory(@RequestParam UUID id) {
        categoryService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
