package com.easy_buy.PRODUCT_SERVICE.service.impl;

import com.easy_buy.PRODUCT_SERVICE.dtos.CategoryDto;
import com.easy_buy.PRODUCT_SERVICE.entity.Category;
import com.easy_buy.PRODUCT_SERVICE.exception.ResourceNotFoundException;
import com.easy_buy.PRODUCT_SERVICE.repositories.CategoryRepository;
import com.easy_buy.PRODUCT_SERVICE.repositories.ProductRepository;
import com.easy_buy.PRODUCT_SERVICE.service.CategoryService;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;


@Service
@Transactional
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepo;
    private final ProductRepository productRepo;

    @Override
    public List<CategoryDto> getAllCategories() {
        return categoryRepo.findAll().stream().map(this::toDto).collect(Collectors.toList());
    }

    @Override
    public CategoryDto getCategoryById(UUID categoryId) {
        return toDto(findCategory(categoryId));
    }

    @Override
    public List<CategoryDto> getCategoriesByProductId(UUID productId) {
        return categoryRepo.findByProductId(productId)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public CategoryDto createCategory(CategoryDto categoryDto) {
        Category category = new Category();
        category.setTitle(categoryDto.getTitle());
        return toDto(categoryRepo.save(category));
    }

    @Override
    public CategoryDto updateCategory(UUID categoryId, CategoryDto categoryDto) {
        Category category = findCategory(categoryId);
        category.setTitle(categoryDto.getTitle());
        return toDto(categoryRepo.save(category));
    }

    @Override
    public void deleteCategory(UUID categoryId) {
        Category category = findCategory(categoryId);
        category.getProducts().forEach(product -> product.getCategories().remove(category));
        category.getProducts().clear();
        categoryRepo.save(category);
        categoryRepo.delete(category);
    }

    private Category findCategory(UUID categoryId) {
        return categoryRepo.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found: " + categoryId));
    }

    private CategoryDto toDto(Category category) {

        CategoryDto dto = new CategoryDto();
        dto.setId(category.getId());
        dto.setTitle(category.getTitle());
        return dto;

    }
}
