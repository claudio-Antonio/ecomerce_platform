package com.example.inventory_service.controllers;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import com.example.inventory_service.domain.Category;
import com.example.inventory_service.domain.Product;
import com.example.inventory_service.dtos.requests.ProductRequest;
import com.example.inventory_service.dtos.responses.ProductResponse;
import com.example.inventory_service.services.ProductService;
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
import java.util.List;
import java.util.UUID;

@ExtendWith(MockitoExtension.class)
class ProductControllerTest {

    @InjectMocks
    private ProductController productController;

    @Mock
    private ProductService productService;

    private UUID productId;
    private Product product;
    private ProductRequest productRequest;

    @BeforeEach
    void setUp() {
        productId = UUID.randomUUID();
        UUID categoryId = UUID.randomUUID();

        Category category = Category.builder()
                .id(categoryId)
                .name("Periféricos")
                .build();

        product = Product.builder()
                .id(productId)
                .name("Teclado Mecânico")
                .description("Switch Blue RGB")
                .price(350.0)
                .stockQuantity(15)
                .reservedQuantity(2)
                .sku("TEC-001")
                .active(true)
                .category(category)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        productRequest = new ProductRequest(
                "Teclado Mecânico",
                "Switch Blue RGB",
                350.0,
                15,
                "TEC-001",
                true,
                categoryId
        );
    }

    @Test
    @DisplayName("Should return 200 with list of products when records exist")
    void listAll_shouldReturn200WithListOfProducts() {
        when(productService.findAll()).thenReturn(List.of(product));

        ResponseEntity<List<ProductResponse>> response = productController.listAll();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(1);
        assertThat(response.getBody().get(0).name()).isEqualTo("Teclado Mecânico");
        assertThat(response.getBody().get(0).availableQuantity()).isEqualTo(13);
        verify(productService, times(1)).findAll();
    }

    @Test
    @DisplayName("Should return 200 with empty list when no records exist")
    void listAll_shouldReturn200WithEmptyList() {
        when(productService.findAll()).thenReturn(List.of());

        ResponseEntity<List<ProductResponse>> response = productController.listAll();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEmpty();
    }

    @Test
    @DisplayName("Should return 200 and product response when found by id")
    void findById_shouldReturn200WhenFound() {
        when(productService.findById(productId)).thenReturn(product);

        ResponseEntity<ProductResponse> response = productController.findById(productId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().id()).isEqualTo(productId);
        assertThat(response.getBody().name()).isEqualTo("Teclado Mecânico");
    }

    @Test
    @DisplayName("Should bubble up RuntimeException when product does not exist")
    void findById_shouldThrowRuntimeExceptionWhenProductDoesNotExist() {
        UUID nonexistentId = UUID.randomUUID();
        when(productService.findById(nonexistentId)).thenThrow(new RuntimeException("Product not found"));

        assertThatThrownBy(() -> productController.findById(nonexistentId))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Product not found");
    }

    @Test
    @DisplayName("Should return 200 and mapped product response when created successfully")
    void createProduct_shouldReturn200WhenCreated() {
        when(productService.create(any(ProductRequest.class))).thenReturn(product);

        ResponseEntity<ProductResponse> response = productController.createProduct(productRequest);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().name()).isEqualTo("Teclado Mecânico");
        verify(productService, times(1)).create(any(ProductRequest.class));
    }

    @Test
    @DisplayName("Should return 200 and updated product response when modification succeeds")
    void updateProduct_shouldReturn200WhenUpdated() {
        when(productService.update(eq(productId), any(ProductRequest.class))).thenReturn(product);

        ResponseEntity<ProductResponse> response = productController.updateProduct(productId, productRequest);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().id()).isEqualTo(productId);
        verify(productService, times(1)).update(eq(productId), any(ProductRequest.class));
    }

    @Test
    @DisplayName("Should return 244 no content when deletion request is completed")
    void deleteProduct_shouldReturn244WhenDeleted() {
        doNothing().when(productService).delete(productId);

        ResponseEntity<Void> response = productController.deleteProduct(productId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(productService, times(1)).delete(productId);
    }
}