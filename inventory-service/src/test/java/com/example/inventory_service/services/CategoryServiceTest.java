package com.example.inventory_service.services;

import com.example.inventory_service.domain.Category;
import com.example.inventory_service.dtos.requests.CategoryRequest;
import com.example.inventory_service.repositories.CategoryRepository;
import org.junit.jupiter.api.BeforeEach;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @InjectMocks
    private CategoryService categoryService;

    @Mock
    private CategoryRepository categoryRepository;

    private UUID categoryId;
    private Category category;
    private CategoryRequest categoryRequest;

    @BeforeEach
    void setUp() {
        categoryId = UUID.randomUUID();

        category = Category.builder()
                .id(categoryId)
                .name("Eletrônicos")
                .description("Dispositivos e gadgets eletrônicos")
                .createdAt(LocalDateTime.now())
                .products(new ArrayList<>())
                .build();

        categoryRequest = new CategoryRequest("Eletrônicos", "Dispositivos e gadgets eletrônicos");
    }

    @Test
    void create_shouldCreateAndReturnCategory() {
        // Given
        given(categoryRepository.save(any(Category.class))).willReturn(category);

        // When
        Category result = categoryService.create(categoryRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(categoryId);
        assertThat(result.getName()).isEqualTo("Eletrônicos");
        assertThat(result.getProducts()).isEmpty();
        then(categoryRepository).should(times(1)).save(any(Category.class));
    }

    @Test
    void findAll_shouldReturnListOfCategories() {
        // Given
        given(categoryRepository.findAll()).willReturn(List.of(category));

        // When
        List<Category> result = categoryService.findAll();

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Eletrônicos");
        then(categoryRepository).should(times(1)).findAll();
    }

    @Test
    void findAll_shouldReturnEmptyListWhenNoCategories() {
        // Given
        given(categoryRepository.findAll()).willReturn(List.of());

        // When
        List<Category> result = categoryService.findAll();

        // Then
        assertThat(result).isEmpty();
        then(categoryRepository).should(times(1)).findAll();
    }

    @Test
    void findById_shouldReturnCategoryWhenFound() {
        // Given
        given(categoryRepository.findById(categoryId)).willReturn(Optional.of(category));

        // When
        Category result = categoryService.findById(categoryId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(categoryId);
        assertThat(result.getName()).isEqualTo("Eletrônicos");
        then(categoryRepository).should(times(1)).findById(categoryId);
    }

    @Test
    void findById_shouldThrowRuntimeExceptionWhenNotFound() {
        // Given
        UUID randomId = UUID.randomUUID();
        given(categoryRepository.findById(randomId)).willReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> categoryService.findById(randomId))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Category not found");
        then(categoryRepository).should(times(1)).findById(randomId);
    }

    @Test
    void update_shouldUpdateAndReturnCategory() {
        // Given
        CategoryRequest updateRequest = new CategoryRequest("Eletrodomésticos", "Nova descrição");
        Category updatedCategory = Category.builder()
                .id(categoryId)
                .name("Eletrodomésticos")
                .description("Nova descrição")
                .createdAt(category.getCreatedAt())
                .products(new ArrayList<>())
                .build();

        given(categoryRepository.findById(categoryId)).willReturn(Optional.of(category));
        given(categoryRepository.save(any(Category.class))).willReturn(updatedCategory);

        // When
        Category result = categoryService.update(categoryId, updateRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Eletrodomésticos");
        assertThat(result.getDescription()).isEqualTo("Nova descrição");
        then(categoryRepository).should(times(1)).findById(categoryId);
        then(categoryRepository).should(times(1)).save(any(Category.class));
    }

    @Test
    void update_shouldThrowExceptionAndNeverSaveWhenCategoryNotFound() {
        UUID randomId = UUID.randomUUID();
        given(categoryRepository.findById(randomId)).willReturn(Optional.empty());

        assertThatThrownBy(() -> categoryService.update(randomId, categoryRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Category not found");

        then(categoryRepository).should(times(1)).findById(randomId);
        then(categoryRepository).should(never()).save(any(Category.class));
    }

    @Test
    void deleteById_shouldCallRepositoryDeleteWhenCategoryExists() {
        given(categoryRepository.findById(categoryId)).willReturn(Optional.of(category));

        categoryService.deleteById(categoryId);

        then(categoryRepository).should(times(1)).findById(categoryId);
        then(categoryRepository).should(times(1)).deleteById(categoryId);
    }

    @Test
    void deleteById_shouldThrowExceptionAndNeverDeleteWhenCategoryNotFound() {
        // Given
        UUID randomId = UUID.randomUUID();
        given(categoryRepository.findById(randomId)).willReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> categoryService.deleteById(randomId))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Category not found");

        then(categoryRepository).should(times(1)).findById(randomId);
        then(categoryRepository).should(never()).deleteById(any(UUID.class));
    }
}