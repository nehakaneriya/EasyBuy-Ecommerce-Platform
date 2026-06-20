package com.easy_buy.PRODUCT_SERVICE.controller;

import com.easy_buy.PRODUCT_SERVICE.dtos.PagedResponse;
import com.easy_buy.PRODUCT_SERVICE.dtos.ProductDto;
import com.easy_buy.PRODUCT_SERVICE.dtos.ProductReviewDto;
import com.easy_buy.PRODUCT_SERVICE.service.ProductService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.server.servlet.context.ServletComponentScan;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
@RequestMapping("/api/products")
@Validated
@RefreshScope
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;


    @GetMapping
    public ResponseEntity<PagedResponse<ProductDto>> getAllProducts(
            @RequestParam(defaultValue = "0") @Min(value = 0, message = "page must be greater than or equal to 0") int page,
            @RequestParam(defaultValue = "12") @Min(value = 1, message = "size must be greater than 0") @Max(value = 100, message = "size must be at most 100") int size
    ) {
        return ResponseEntity.ok(productService.getAllProducts(page, size));
    }

    // Post /api/products - Create a new product
    @PostMapping
    public ResponseEntity<ProductDto> createProduct(@Valid @RequestBody ProductDto productDto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(productService.createProduct(productDto));
    }

    // GET /api/products/{productId} - Get product by ID
    @GetMapping("/{productId}")
    public ResponseEntity<ProductDto> getProductById(@PathVariable UUID productId) {
        return ResponseEntity.ok(productService.getProductById(productId));
    }

    // GET /api/products/category/{categoryId} - Get products by category ID with pagination
    @GetMapping("/category/{categoryId}")
    public ResponseEntity<PagedResponse<ProductDto>> getProductsByCategoryId(
            @PathVariable UUID categoryId,
            @RequestParam(defaultValue = "0") @Min(value = 0, message = "page must be greater than or equal to 0") int page,
            @RequestParam(defaultValue = "12") @Min(value = 1, message = "size must be greater than 0") @Max(value = 100, message = "size must be at most 100") int size
    ) {
        return ResponseEntity.ok(productService.getProductsByCategoryId(categoryId, page, size));
    }


    // PUT /api/products/{productId} - Update product details
    @PutMapping("/{productId}")
    public ResponseEntity<ProductDto> updateProduct(@PathVariable UUID productId, @Valid @RequestBody ProductDto productDto) {
        return ResponseEntity.ok(productService.updateProduct(productId, productDto));
    }

    // DELETE /api/products/{productId} - Delete product by ID
    @DeleteMapping("/{productId}")
    public ResponseEntity<Void> deleteProduct(@PathVariable UUID productId) {
        productService.deleteProduct(productId);
        return ResponseEntity.noContent().build();
    }

    // POST /api/products/{productId}/categories/{categoryId} - Add category to product
    @PostMapping("/{productId}/categories/{categoryId}")
    public ResponseEntity<ProductDto> addCategoryToProduct(@PathVariable UUID productId, @PathVariable UUID categoryId) {
        return ResponseEntity.ok(productService.addCategoryToProduct(productId, categoryId));
    }

    // DELETE /api/products/{productId}/categories/{categoryId} - Remove category from product
    @DeleteMapping("/{productId}/categories/{categoryId}")
    public ResponseEntity<ProductDto> removeCategoryFromProduct(@PathVariable UUID productId, @PathVariable UUID categoryId) {
        return ResponseEntity.ok(productService.removeCategoryFromProduct(productId, categoryId));
    }

    // POST /api/products/{productId}/reviews - Add a review to a product
    @PostMapping("/{productId}/reviews")
    public ResponseEntity<ProductReviewDto> addReviewToProduct(@PathVariable UUID productId, @Valid @RequestBody ProductReviewDto reviewDto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(productService.addReviewToProduct(productId, reviewDto));
    }

   // POST /api/products/{productId}/images - Upload images for a product
    @PostMapping(value = "/{productId}/images", consumes = "multipart/form-data")
    public ResponseEntity<ProductDto> addProductImages(
            @PathVariable UUID productId,
            @RequestParam("files") java.util.List<MultipartFile> files
    ) {
        return ResponseEntity.ok(productService.addProductImages(productId, files));
    }

    // GET /api/products/{productId}/images - Get all image URLs for a product
    @GetMapping("/{productId}/images")
    public ResponseEntity<java.util.List<String>> getProductImages(@PathVariable UUID productId) {
        return ResponseEntity.ok(productService.getProductImages(productId));
    }

}
