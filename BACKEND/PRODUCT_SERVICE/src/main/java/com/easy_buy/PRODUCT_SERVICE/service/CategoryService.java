package com.easy_buy.PRODUCT_SERVICE.service;

import com.easy_buy.PRODUCT_SERVICE.dtos.CategoryDto;

import java.util.List;
import java.util.UUID;

public interface CategoryService {

    // Get all categories from a database
    List<CategoryDto> getAllCategories();

    // Get single category using category ID
    CategoryDto getCategoryById(UUID categoryId);

    // Get categories related to a specific product
    List<CategoryDto> getCategoriesByProductId(UUID productId);

    // Create a New category
    CategoryDto createCategory(CategoryDto categoryDto);

    // Update existing category using category ID
    CategoryDto updateCategory(UUID categoryId, CategoryDto categoryDto);

    // Delete category using category ID
    void deleteCategory(UUID categoryId);
}
