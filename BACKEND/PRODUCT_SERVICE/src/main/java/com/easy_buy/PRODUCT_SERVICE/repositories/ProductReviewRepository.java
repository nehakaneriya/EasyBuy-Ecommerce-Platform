package com.easy_buy.PRODUCT_SERVICE.repositories;

import com.easy_buy.PRODUCT_SERVICE.entity.Product;
import com.easy_buy.PRODUCT_SERVICE.entity.ProductReview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;


@Repository
public interface ProductReviewRepository extends JpaRepository<ProductReview, Long> {


    //method to get product buy product id and product
    List<ProductReview> findByProduct(Product product);

    List<ProductReview> findByProduct_Id(UUID productId);
}