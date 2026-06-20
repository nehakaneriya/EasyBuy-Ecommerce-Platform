package com.easy_buy.CART_ORDER_SERVICE.controller;

import com.easy_buy.CART_ORDER_SERVICE.payload.request.CheckoutRequest;
import com.easy_buy.CART_ORDER_SERVICE.payload.response.OrderResponse;
import com.easy_buy.CART_ORDER_SERVICE.service.OrderService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/orders")
@AllArgsConstructor
public class OrderController {

    private final OrderService orderService;

    // Checkout cart and place order
    @PostMapping("/checkout/{userId}")
    public ResponseEntity<OrderResponse> checkout(@PathVariable UUID userId,
                                                  @Valid @RequestBody CheckoutRequest request)
    {
        return ResponseEntity.ok(orderService.checkout(userId, request));
    }

    // Get order by order id
    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponse> getOrderById(@PathVariable UUID orderId)
    {
        return ResponseEntity.ok(orderService.getOrderById(orderId));
    }

    // Get order by order number
    @GetMapping("/number/{orderNumber}")
    public ResponseEntity<OrderResponse> getOrderByNumber(@PathVariable String orderNumber)
    {
        return ResponseEntity.ok(orderService.getOrderByNumber(orderNumber));
    }

    // Get all orders of user
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<OrderResponse>> getOrdersByUserId(@PathVariable UUID userId)
    {
        return ResponseEntity.ok(orderService.getOrdersByUserId(userId));
    }

    // Cancel order
    @DeleteMapping("/{orderId}")
    public ResponseEntity<OrderResponse> cancelOrder(@PathVariable UUID orderId) {
        return ResponseEntity.ok(orderService.cancelOrder(orderId));
    }
}