package com.easy_buy.CART_ORDER_SERVICE.repository;

import com.easy_buy.CART_ORDER_SERVICE.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface CartItemRepository extends JpaRepository<CartItem, UUID> {

    // Method to find a CartItem by cartId and productId
    Optional<CartItem> findByCart_CartIdAndProductId(
            UUID cartId,
            UUID productId
    );
}