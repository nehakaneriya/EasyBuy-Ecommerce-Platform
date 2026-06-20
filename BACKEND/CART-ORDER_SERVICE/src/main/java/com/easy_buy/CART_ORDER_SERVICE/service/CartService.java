package com.easy_buy.CART_ORDER_SERVICE.service;

import com.easy_buy.CART_ORDER_SERVICE.payload.request.AddToCartRequest;
import com.easy_buy.CART_ORDER_SERVICE.payload.request.UpdateCartRequest;
import com.easy_buy.CART_ORDER_SERVICE.payload.response.CartResponse;

import java.util.UUID;

public interface CartService {

    // Retrieves the current state of the user's cart
    CartResponse getCart(UUID userId);

    // Adds an item to the user's cart
    CartResponse addItem(UUID userId,AddToCartRequest request);

    // Updates the quantity of an existing item in the user's cart
    CartResponse updateItem(UUID userId,UUID productId,UpdateCartRequest request);

    // Removes an item from the user's cart
    CartResponse removeItem(UUID userId,UUID productId);

    // Clears all items from the user's cart
    void clearCart(UUID userId);
}