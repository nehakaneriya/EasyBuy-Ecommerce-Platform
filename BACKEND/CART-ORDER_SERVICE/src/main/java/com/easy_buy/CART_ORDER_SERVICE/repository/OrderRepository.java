package com.easy_buy.CART_ORDER_SERVICE.repository;

import com.easy_buy.CART_ORDER_SERVICE.entity.Order;
import com.easy_buy.CART_ORDER_SERVICE.entity.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OrderRepository extends JpaRepository<Order, UUID> {

    // Method to find all orders for a specific user, ordered by creation date (newest first)
    List<Order> findByUserIdOrderByCreatedAtDesc(UUID userId);

    // Method to find all orders with a specific order status
    List<Order> findByOrderStatus(OrderStatus orderStatus);

    // Method to find an order by its unique order number
    Optional<Order> findByOrderNumber(String orderNumber);
}
