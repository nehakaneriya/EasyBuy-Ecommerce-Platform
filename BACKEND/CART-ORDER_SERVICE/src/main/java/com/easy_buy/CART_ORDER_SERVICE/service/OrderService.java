package com.easy_buy.CART_ORDER_SERVICE.service;

import com.easy_buy.CART_ORDER_SERVICE.payload.request.CheckoutRequest;
import com.easy_buy.CART_ORDER_SERVICE.payload.response.OrderResponse;

import java.util.List;
import java.util.UUID;

public interface OrderService {

    // Processes the checkout for a user, creating an order and reserving stock
    OrderResponse checkout(UUID userId, CheckoutRequest request);

    // Retrieves an order by its unique identifier
    OrderResponse getOrderById(UUID orderId);

    // Retrieves an order by its order number
    OrderResponse getOrderByNumber(String orderNumber);

    // Retrieves all orders associated with a specific user
    List<OrderResponse> getOrdersByUserId(UUID userId);

    // Cancels an order by its unique identifier, releasing reserved stock and updating order status
    OrderResponse cancelOrder(UUID orderId);

    // Releases reserved stock for a specific product and quantity, typically called when an order is canceled or fails
    void releaseReservedStock(UUID productId,Integer quantity);
}
