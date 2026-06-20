package com.easy_buy.PRODUCT_SERVICE.service.impl;

import com.easy_buy.PRODUCT_SERVICE.dtos.ProductDto;
import com.easy_buy.PRODUCT_SERVICE.dtos.ProductReviewDto;
import com.easy_buy.PRODUCT_SERVICE.entity.Product;
import com.easy_buy.PRODUCT_SERVICE.entity.ProductReview;
import com.easy_buy.PRODUCT_SERVICE.exception.ResourceNotFoundException;
import com.easy_buy.PRODUCT_SERVICE.repositories.ProductRepository;
import com.easy_buy.PRODUCT_SERVICE.repositories.ProductReviewRepository;
import com.easy_buy.PRODUCT_SERVICE.service.ReviewService;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private final ProductReviewRepository reviewRepo;
    private final ProductRepository productRepo;

    @Override
    public List<ProductReviewDto> getAllReviews() {
        return reviewRepo.findAll().stream().map(this::toDto).collect(Collectors.toList());
    }

    @Override
    public ProductReviewDto getReviewById(Long reviewId) {
        return toDto(findReview(reviewId));
    }

    @Override
    public List<ProductReviewDto> getReviewsByProductId(UUID productId) {
        return reviewRepo.findByProduct_Id(productId).stream().map(this::toDto).collect(Collectors.toList());
    }

    @Override
    public ProductReviewDto createReview(UUID productId, ProductReviewDto reviewDto) {
        Product product = findProduct(productId);
        ProductReview review = new ProductReview();
        review.setTitle(reviewDto.getTitle());
        review.setComment(reviewDto.getComment());
        review.setRating(reviewDto.getRating());
        review.setProduct(product);
        return toDto(reviewRepo.save(review));
    }

    @Override
    public ProductReviewDto updateReview(Long reviewId, ProductReviewDto reviewDto) {
        ProductReview review = findReview(reviewId);
        review.setTitle(reviewDto.getTitle());
        review.setComment(reviewDto.getComment());
        review.setRating(reviewDto.getRating());
        return toDto(reviewRepo.save(review));
    }

    @Override
    public void deleteReview(Long reviewId) {
        ProductReview review = findReview(reviewId);
        reviewRepo.delete(review);
    }

    private Product findProduct(UUID productId) {
        return productRepo.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + productId));
    }

    private ProductReview findReview(Long reviewId) {
        return reviewRepo.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found: " + reviewId));
    }

    private ProductReviewDto toDto(ProductReview review) {
        ProductReviewDto dto = new ProductReviewDto();
        dto.setId(review.getId());
        dto.setTitle(review.getTitle());
        dto.setComment(review.getComment());
        dto.setRating(review.getRating());
        if (review.getProduct() != null) {
            dto.setProduct(toProductDto(review.getProduct()));
        }
        return dto;
    }

    private ProductDto toProductDto(Product product) {
        ProductDto dto = new ProductDto();
        dto.setId(product.getId());
        dto.setTitle(product.getTitle());
        return dto;
    }
}