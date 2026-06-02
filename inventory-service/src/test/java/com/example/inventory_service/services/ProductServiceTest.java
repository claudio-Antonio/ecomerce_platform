package com.example.inventory_service.services;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.example.inventory_service.domain.Category;
import com.example.inventory_service.domain.Product;
import com.example.inventory_service.dtos.requests.ProductRequest;
import com.example.inventory_service.repositories.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @InjectMocks
    private ProductService productService;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CategoryService categoryService;

    private UUID productId;
    private UUID categoryId;
    private Category category;
    private Product product;
    private ProductRequest productRequest;

    @BeforeEach
    void setUp() {
        productId = UUID.randomUUID();
        categoryId = UUID.randomUUID();

        category = Category.builder()
                .id(categoryId)
                .name("Componentes")
                .products(new ArrayList<>())
                .build();

        product = Product.builder()
                .id(productId)
                .name("Processador")
                .description("Processador de última geração")
                .price(1500.0)
                .stockQuantity(10)
                .reservedQuantity(0)
                .sku("CPU-001")
                .active(true)
                .category(category)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        productRequest = new ProductRequest(
                "Processador",
                "Processador de última geração",
                1500.0,
                10,
                "CPU-001",
                true,
                categoryId
        );
    }

    @Test
    @DisplayName("Should create and return product when payload is valid")
    void create_shouldCreateAndReturnProduct() {
        when(categoryService.findById(categoryId)).thenReturn(category);
        when(productRepository.save(any(Product.class))).thenReturn(product);

        Product result = productService.create(productRequest);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(productId);
        assertThat(result.getName()).isEqualTo("Processador");
        verify(categoryService, times(1)).findById(categoryId);
        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Test
    @DisplayName("Should return list containing products when registered")
    void findAll_shouldReturnListOfProducts() {
        when(productRepository.findAll()).thenReturn(List.of(product));

        List<Product> result = productService.findAll();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Processador");
        verify(productRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Should return empty list when no products are registered")
    void findAll_shouldReturnEmptyListWhenNoProducts() {
        when(productRepository.findAll()).thenReturn(List.of());

        List<Product> result = productService.findAll();

        assertThat(result).isEmpty();
        verify(productRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Should return product when found by id")
    void findById_shouldReturnProductWhenFound() {
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));

        Product result = productService.findById(productId);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(productId);
        verify(productRepository, times(1)).findById(productId);
    }

    @Test
    @DisplayName("Should throw RuntimeException when product not found by id")
    void findById_shouldThrowRuntimeExceptionWhenNotFound() {
        UUID randomId = UUID.randomUUID();
        when(productRepository.findById(randomId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.findById(randomId))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Product not found");
        verify(productRepository, times(1)).findById(randomId);
    }

    @Test
    @DisplayName("Should return correct price of product")
    void getPrice_shouldReturnProductPriceWhenFound() {
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));

        Double price = productService.getPrice(productId);

        assertThat(price).isEqualTo(1500.0);
        verify(productRepository, times(1)).findById(productId);
    }

    @Test
    @DisplayName("Should update and return updated product")
    void update_shouldUpdateAndReturnProduct() {
        ProductRequest updateRequest = new ProductRequest("Novo Nome", "Nova Desc", 1600.0, 15, "CPU-002", true, categoryId);
        Product updatedProduct = Product.builder().id(productId).name("Novo Nome").price(1600.0).category(category).build();

        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(categoryService.findById(categoryId)).thenReturn(category);
        when(productRepository.save(any(Product.class))).thenReturn(updatedProduct);

        Product result = productService.update(productId, updateRequest);

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Novo Nome");
        verify(productRepository, times(1)).findById(productId);
        verify(categoryService, times(1)).findById(categoryId);
        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Test
    @DisplayName("Should throw exception and never save when updating non-existent product")
    void update_shouldThrowExceptionAndNeverSaveWhenProductNotFound() {
        UUID randomId = UUID.randomUUID();
        when(productRepository.findById(randomId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.update(randomId, productRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Product not found");

        verify(productRepository, times(1)).findById(randomId);
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    @DisplayName("Should delete product when found by id")
    void delete_shouldDeleteProductWhenFound() {
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        doNothing().when(productRepository).delete(product);

        productService.delete(productId);

        verify(productRepository, times(1)).findById(productId);
        verify(productRepository, times(1)).delete(product);
    }

    @Test
    @DisplayName("Should throw exception and never delete when product not found")
    void delete_shouldThrowExceptionAndNeverDeleteWhenProductNotFound() {
        UUID randomId = UUID.randomUUID();
        when(productRepository.findById(randomId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.delete(randomId))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Product not found");

        verify(productRepository, times(1)).findById(randomId);
        verify(productRepository, never()).delete(any(Product.class));
    }
}