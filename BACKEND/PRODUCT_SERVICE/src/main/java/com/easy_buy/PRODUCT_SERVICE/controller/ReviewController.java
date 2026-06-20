package com.easy_buy.PRODUCT_SERVICE.controller;


import com.easy_buy.PRODUCT_SERVICE.dtos.ProductReviewDto;
import com.easy_buy.PRODUCT_SERVICE.service.ReviewService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/reviews")
@Validated
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    //POST /api/reviews/product/{productId} - Create a new review for a product
    @PostMapping("/product/{productId}")
    public ResponseEntity<ProductReviewDto> createReview(@PathVariable UUID productId, @Valid @RequestBody ProductReviewDto reviewDto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(reviewService.createReview(productId, reviewDto));
    }

    //GET /api/reviews - Get all reviews
    @GetMapping
    public ResponseEntity<List<ProductReviewDto>> getAllReviews() {
        return ResponseEntity.ok(reviewService.getAllReviews());
    }

    //GET /api/reviews/{reviewId} - Get review by ID
    @GetMapping("/{reviewId}")
    public ResponseEntity<ProductReviewDto> getReviewById(@PathVariable Long reviewId) {
        return ResponseEntity.ok(reviewService.getReviewById(reviewId));
    }

    //GET /api/reviews/product/{productId} - Get reviews by product ID
    @GetMapping("/product/{productId}")
    public ResponseEntity<List<ProductReviewDto>> getReviewsByProductId(@PathVariable UUID productId) {
        return ResponseEntity.ok(reviewService.getReviewsByProductId(productId));
    }

    //PUT /api/reviews/{reviewId} - Update review by ID
    @PutMapping("/{reviewId}")
    public ResponseEntity<ProductReviewDto> updateReview(@PathVariable Long reviewId, @Valid @RequestBody ProductReviewDto reviewDto) {
        return ResponseEntity.ok(reviewService.updateReview(reviewId, reviewDto));
    }

    //DELETE /api/reviews/{reviewId} - Delete review by ID
    @DeleteMapping("/{reviewId}")
    public ResponseEntity<Void> deleteReview(@PathVariable Long reviewId) {
        reviewService.deleteReview(reviewId);
        return ResponseEntity.noContent().build();
    }
}
