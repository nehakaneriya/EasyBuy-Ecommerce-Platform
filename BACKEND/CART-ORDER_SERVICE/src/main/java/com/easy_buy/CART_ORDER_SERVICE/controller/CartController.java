package com.easy_buy.CART_ORDER_SERVICE.controller;

import com.easy_buy.CART_ORDER_SERVICE.payload.request.AddToCartRequest;
import com.easy_buy.CART_ORDER_SERVICE.payload.request.UpdateCartRequest;
import com.easy_buy.CART_ORDER_SERVICE.payload.response.CartResponse;
import com.easy_buy.CART_ORDER_SERVICE.service.CartService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/carts")
@AllArgsConstructor
public class CartController {

    private final CartService cartService;

    // Get active cart of user
    @GetMapping("/{userId}")
    public ResponseEntity<CartResponse> getCart(@PathVariable UUID userId)
    {
        return ResponseEntity.ok(cartService.getCart(userId));
    }

    // Add product into cart
    @PostMapping("/{userId}/items")
    public ResponseEntity<CartResponse> addItem(@PathVariable UUID userId,
                                                @Valid @RequestBody AddToCartRequest request)
    {
        return ResponseEntity.ok(cartService.addItem(userId, request));
    }

    // Update cart item quantity
    @PutMapping("/{userId}/items/{productId}")
    public ResponseEntity<CartResponse> updateItem(@PathVariable UUID userId, @PathVariable UUID productId, @RequestBody UpdateCartRequest request)
    {
        return ResponseEntity.ok(cartService.updateItem(userId, productId, request));
    }

    // Remove specific product from cart
    @DeleteMapping("/{userId}/items/{productId}")
    public ResponseEntity<CartResponse> removeItem(@PathVariable UUID userId, @PathVariable UUID productId)
    {
        return ResponseEntity.ok(cartService.removeItem(userId, productId));
    }

    // Clear complete cart
    @DeleteMapping("/{userId}/clear")
    public ResponseEntity<String> clearCart(@PathVariable UUID userId)
    {
        cartService.clearCart(userId);
        return ResponseEntity.ok("Cart cleared successfully");
    }
}