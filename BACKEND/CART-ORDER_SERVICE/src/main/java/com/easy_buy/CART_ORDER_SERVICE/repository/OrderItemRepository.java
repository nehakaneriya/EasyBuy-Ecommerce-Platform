package com.easy_buy.CART_ORDER_SERVICE.repository;

import com.easy_buy.CART_ORDER_SERVICE.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface OrderItemRepository extends JpaRepository<OrderItem, UUID> {

    // Method to find all OrderItems for a specific order
    List<OrderItem> findByOrder_OrderId(UUID orderId);
}
