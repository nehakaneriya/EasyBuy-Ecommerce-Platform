package com.easy_buy.PRODUCT_SERVICE.repositories;

import com.easy_buy.PRODUCT_SERVICE.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CategoryRepository extends JpaRepository<Category, UUID> {

    @Query("SELECT c FROM Category c JOIN c.products p WHERE p.id = :productId")
     List<Category> findByProductId(@Param("productId") UUID productId);
}
