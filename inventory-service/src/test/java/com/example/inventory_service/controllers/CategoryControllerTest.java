package com.example.inventory_service.controllers;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import com.example.inventory_service.domain.Category;
import com.example.inventory_service.dtos.requests.CategoryRequest;
import com.example.inventory_service.dtos.responses.CategoryResponse;
import com.example.inventory_service.services.CategoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@ExtendWith(MockitoExtension.class)
class CategoryControllerTest {

    @InjectMocks
    private CategoryController categoryController;

    @Mock
    private CategoryService categoryService;

    private UUID categoryId;
    private Category category;
    private CategoryRequest categoryRequest;

    @BeforeEach
    void setUp() {
        categoryId = UUID.randomUUID();

        category = Category.builder()
                .id(categoryId)
                .name("Eletro")
                .description("Produtos eletrônicos diversos")
                .createdAt(LocalDateTime.now())
                .products(new ArrayList<>())
                .build();

        categoryRequest = new CategoryRequest("Eletro", "Produtos eletrônicos diversos");
    }

    @Test
    @DisplayName("Should return 200 with list of categories when records exist")
    void findAll_shouldReturn200WithListOfCategories() {
        when(categoryService.findAll()).thenReturn(List.of(category));

        ResponseEntity<List<CategoryResponse>> response = categoryController.findAll();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(1);
        assertThat(response.getBody().get(0).name()).isEqualTo("Eletro");
        verify(categoryService, times(1)).findAll();
    }

    @Test
    @DisplayName("Should return 200 with empty list when no categories exist")
    void findAll_shouldReturn200WithEmptyList() {
        when(categoryService.findAll()).thenReturn(List.of());

        ResponseEntity<List<CategoryResponse>> response = categoryController.findAll();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEmpty();
    }

    @Test
    @DisplayName("Should return 200 and category response when found by id")
    void findById_shouldReturn200WhenFound() {
        when(categoryService.findById(categoryId)).thenReturn(category);

        ResponseEntity<CategoryResponse> response = categoryController.findById(categoryId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().id()).isEqualTo(categoryId);
        assertThat(response.getBody().name()).isEqualTo("Eletro");
    }

    @Test
    @DisplayName("Should bubble up RuntimeException when category does not exist")
    void findById_shouldThrowRuntimeExceptionWhenCategoryDoesNotExist() {
        UUID nonexistentId = UUID.randomUUID();
        when(categoryService.findById(nonexistentId)).thenThrow(new RuntimeException("Category not found"));

        assertThatThrownBy(() -> categoryController.findById(nonexistentId))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Category not found");
    }

    @Test
    @DisplayName("Should return 200 and mapped category response when created successfully")
    void saveCategory_shouldReturn200WhenCreated() {
        when(categoryService.create(any(CategoryRequest.class))).thenReturn(category);

        ResponseEntity<CategoryResponse> response = categoryController.saveCategory(categoryRequest);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().name()).isEqualTo("Eletro");
        verify(categoryService, times(1)).create(any(CategoryRequest.class));
    }

    @Test
    @DisplayName("Should return 200 and updated category response when modification succeeds")
    void updateCategory_shouldReturn200WhenUpdated() {
        when(categoryService.update(eq(categoryId), any(CategoryRequest.class))).thenReturn(category);

        ResponseEntity<CategoryResponse> response = categoryController.updateCategory(categoryId, categoryRequest);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().id()).isEqualTo(categoryId);
        verify(categoryService, times(1)).update(eq(categoryId), any(CategoryRequest.class));
    }

    @Test
    @DisplayName("Should return 204 no content when deletion request is completed")
    void deleteCategory_shouldReturn244WhenDeleted() {
        doNothing().when(categoryService).deleteById(categoryId);

        ResponseEntity<Void> response = categoryController.deleteCategory(categoryId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(categoryService, times(1)).deleteById(categoryId);
    }
}