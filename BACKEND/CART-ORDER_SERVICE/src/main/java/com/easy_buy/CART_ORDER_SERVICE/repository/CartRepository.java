package com.easy_buy.CART_ORDER_SERVICE.repository;

import com.easy_buy.CART_ORDER_SERVICE.entity.Cart;
import com.easy_buy.CART_ORDER_SERVICE.entity.CartStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface CartRepository extends JpaRepository<Cart, UUID> {

    Optional<Cart> findByUserIdAndCartStatus(UUID userId, CartStatus cartStatus);
}