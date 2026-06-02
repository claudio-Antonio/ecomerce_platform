package com.example.inventory_service.services;

import com.example.inventory_service.domain.Category;
import com.example.inventory_service.dtos.requests.CategoryRequest;
import com.example.inventory_service.repositories.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class CategoryService {
    private final CategoryRepository categoryRepository;

    public Category create(CategoryRequest data) {
        Category newCategory = Category.builder()
                .name(data.name())
                .description(data.description())
                .createdAt(LocalDateTime.now())
                .products(new ArrayList<>())
                .build();

        return categoryRepository.save(newCategory);
    }

    public List<Category> findAll() {
        return categoryRepository.findAll();
    }

    public Category findById(UUID id) {
        return categoryRepository.findById(id).orElseThrow(() -> new RuntimeException("Category not found"));
    }

    public Category update(UUID id, CategoryRequest data) {
        Category category = findById(id);

        category.setName(data.name());
        category.setDescription(data.description());
        return categoryRepository.save(category);
    }

    public void deleteById(UUID id) {
        findById(id);
        categoryRepository.deleteById(id);
    }
}
