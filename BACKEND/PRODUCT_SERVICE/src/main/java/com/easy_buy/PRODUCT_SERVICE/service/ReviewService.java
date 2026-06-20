package com.easy_buy.PRODUCT_SERVICE.service;

import com.easy_buy.PRODUCT_SERVICE.dtos.ProductReviewDto;


import java.util.List;
import java.util.UUID;

public interface ReviewService {


    // This method is used to get all reviews from a database
    List<ProductReviewDto> getAllReviews();

    // This method is used to get single review using review ID
    ProductReviewDto getReviewById(Long reviewId);

    // This method is used to get all reviews of a specific product
    List<ProductReviewDto> getReviewsByProductId(UUID productId);

    // This method is used to create review for a product
    ProductReviewDto createReview(UUID productId, ProductReviewDto reviewDto);

    // This method is used to update existing review
    ProductReviewDto updateReview(Long reviewId, ProductReviewDto reviewDto);

    // This method is used to delete review using review ID
    void deleteReview(Long reviewId);
}
