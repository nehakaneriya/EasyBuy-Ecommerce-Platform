package com.easy_buy.CART_ORDER_SERVICE.service.impl;

import com.easy_buy.CART_ORDER_SERVICE.client.InventoryFeignClient;
import com.easy_buy.CART_ORDER_SERVICE.client.ProductFeignClient;
import com.easy_buy.CART_ORDER_SERVICE.dtos.InventorySnapshot;
import com.easy_buy.CART_ORDER_SERVICE.entity.Cart;
import com.easy_buy.CART_ORDER_SERVICE.entity.CartItem;
import com.easy_buy.CART_ORDER_SERVICE.entity.CartStatus;
import com.easy_buy.CART_ORDER_SERVICE.exception.BusinessRuleException;
import com.easy_buy.CART_ORDER_SERVICE.exception.ExternalServiceException;
import com.easy_buy.CART_ORDER_SERVICE.exception.ResourceNotFoundException;
import com.easy_buy.CART_ORDER_SERVICE.payload.request.AddToCartRequest;
import com.easy_buy.CART_ORDER_SERVICE.payload.request.UpdateCartRequest;
import com.easy_buy.CART_ORDER_SERVICE.payload.response.CartItemResponse;
import com.easy_buy.CART_ORDER_SERVICE.payload.response.CartResponse;
import com.easy_buy.CART_ORDER_SERVICE.repository.CartRepository;
import com.easy_buy.CART_ORDER_SERVICE.service.CartService;
import com.easy_buy.COMMON_SERVICE.payload.ProductSnapshot;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final ProductFeignClient productClient;
    private final InventoryFeignClient inventoryClient;


    @Override
    public CartResponse getCart(UUID userId) {

        Cart cart = getOrCreateActiveCart(userId);
        return toResponse(cart);
    }

    @Override
    public CartResponse addItem(UUID userId, AddToCartRequest request) {

        // Get active cart or create new
        Cart cart = getOrCreateActiveCart(userId);

        // Fetch product from product service
        ProductSnapshot product = fetchProduct(request.getProductId());

        // Fetch inventory
        InventorySnapshot inventory;
        try {
            inventory = inventoryClient.getByProductId(request.getProductId());
        } catch (Exception ex) {
            throw new ExternalServiceException("Failed to load inventory for product " + request.getProductId(), ex);
        }

        // Validate stock
        // Account for any quantity already in the cart for this product
        int alreadyInCart = cart.getCartItems()
                .stream()
                .filter(ci -> ci.getProductId().equals(request.getProductId()))
                .mapToInt(ci -> safeQuantity(ci.getQuantity()))
                .findFirst()
                .orElse(0);

        int totalRequested = alreadyInCart + request.getQuantity();

        if (inventory.getAvailableQuantity() == null || inventory.getAvailableQuantity() < totalRequested) {
            throw new BusinessRuleException(
                    "Insufficient stock. Available: "
                            + (inventory.getAvailableQuantity() == null ? 0 : inventory.getAvailableQuantity())
                            + ", Requested total: " + totalRequested
            );
        }
        // Check product already exists in cart
        CartItem item = cart
                .getCartItems()
                .stream()
                .filter(existing ->
                        existing.getProductId()
                                .equals(
                                        request.getProductId()
                                )
                )
                .findFirst()
                .orElseGet(() -> {
                    CartItem created = CartItem
                            .builder()
                            .cart(cart)
                            .productId(request.getProductId())
                            .build();
                    cart.getCartItems()
                            .add(created);
                    return created;
                });

        // Set latest product details
        item.setProductTitle(product.getTitle());
        item.setProductImage(product.getProductImages() == null || product.getProductImages().isEmpty() ? "" : product.getProductImages().get(0));
        item.setUnitPrice(BigDecimal.valueOf(product.getPrice() == null ? 0.0 : product.getPrice()).setScale(2, RoundingMode.HALF_UP));
        item.setDiscountPercent(defaultZero(product.getDiscount()));

        // Increase quantity
        item.setQuantity(safeQuantity(item.getQuantity()) + request.getQuantity());

        // Save cart
        Cart savedCart = cartRepository.save(cart);
        return toResponse(savedCart);
    }

    @Override
    public CartResponse updateItem(UUID userId, UUID productId, UpdateCartRequest request) {
        // Get active cart
        Cart cart = getOrCreateActiveCart(userId);

        // Find cart item
        CartItem item = findCartItem(cart, productId);

        // Validate stock before updating quantity
        InventorySnapshot inventory;
        try {
            inventory = inventoryClient.getByProductId(productId);
        } catch (Exception ex) {
            throw new ExternalServiceException("Failed to load inventory for product " + productId, ex);
        }
        if (inventory.getAvailableQuantity() == null || inventory.getAvailableQuantity() < request.getQuantity()) {
            throw new BusinessRuleException(
                    "Insufficient stock. Available: "
                            + (inventory.getAvailableQuantity() == null ? 0 : inventory.getAvailableQuantity())
                            + ", Requested: " + request.getQuantity()
            );
        }

        // Update quantity
        item.setQuantity(request.getQuantity());

        // Save updated cart
        Cart savedCart = cartRepository.save(cart);
        return toResponse(savedCart);
    }


    //Remove specific product item from user's cart
    @Override
    public CartResponse removeItem(UUID userId, UUID productId) {
        // Get active cart
        Cart cart = getOrCreateActiveCart(userId);

        // Find cart item
        CartItem item = findCartItem(cart, productId);

        // Remove item from cart
        cart.getCartItems().remove(item);

        // Save updated cart
        Cart savedCart = cartRepository.save(cart);
        return toResponse(savedCart);
    }

    // Clear all items from user's active cart
    @Override
    public void clearCart(UUID userId) {
        // Get active cart of user
        Cart cart = getOrCreateActiveCart(userId);

        // Remove all cart items
        cart.getCartItems().clear();

        // Reset cart total
        cart.setCartTotal(BigDecimal.ZERO);

        // Save updated cart
        cartRepository.save(cart);
    }

    // Helper method to get or create an active cart for the user
    private Cart getOrCreateActiveCart(UUID userId) {
        if (userId == null) {
            throw new BusinessRuleException("userId is required");
        }
        return cartRepository
                .findByUserIdAndCartStatus(userId, CartStatus.ACTIVE)
                .orElseGet(() -> {
                    Cart cart = Cart.builder()
                            .userId(userId)
                            .cartStatus(CartStatus.ACTIVE)
                            .cartItems(new ArrayList<>())
                            .cartTotal(BigDecimal.ZERO)
                            .build();
                    return cartRepository.save(cart);
                });

    }

    // Helper method to find a cart item by productId
    private CartItem findCartItem(Cart cart, UUID productId)
    {
        return cart.getCartItems()
                .stream()
                .filter(item ->
                        item.getProductId()
                                .equals(productId)
                )
                .findFirst()
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Cart item not found"
                        )
                );
    }

    // Helper method to convert Cart entity to CartResponse DTO
    private CartResponse toResponse(Cart cart)
    {
        List<CartItemResponse> items = cart
                .getCartItems()
                .stream()
                .map(this::toItemResponse)
                .toList();
        BigDecimal total = items
                .stream()
                .map(CartItemResponse::getLineTotal)
                .reduce(
                        BigDecimal.ZERO,
                        BigDecimal::add
                );
        return CartResponse.builder()
                .cartId(cart.getCartId())
                .userId(cart.getUserId())
                .cartStatus(cart.getCartStatus())
                .cartTotal(total)
                .createdAt(cart.getCreatedAt())
                .updatedAt(cart.getUpdatedAt())
                .cartItems(items)
                .build();
    }

    // Helper method to convert CartItem entity to CartItemResponse DTO
    private CartItemResponse toItemResponse(CartItem item)
    {
        return CartItemResponse.builder()
                .cartItemId(item.getCartItemId())
                .productId(item.getProductId())
                .productTitle(item.getProductTitle())
                .productImage(item.getProductImage())
                .unitPrice(item.getUnitPrice())
                .discountPercent(item.getDiscountPercent())
                .quantity(item.getQuantity())
                .lineTotal(item.getLineTotal())
                .build();
    }

    private ProductSnapshot fetchProduct(UUID productId) {
        try {
            ProductSnapshot product = productClient.getProductById(productId);
            if (product == null) {
                throw new ExternalServiceException("Product service unavailable, could not fetch product: " + productId);
            }
            if (Boolean.FALSE.equals(product.getLive())) {
                throw new BusinessRuleException("Product is not live/available: " + productId);
            }
            return product;
        } catch (BusinessRuleException | ExternalServiceException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new ExternalServiceException("Failed to load product " + productId, ex);
        }
    }


    // Handle null quantity
    private int safeQuantity(Integer quantity) {
        return quantity == null ? 0 : quantity;
    }

    //Handle null integer values
    private int defaultZero(Integer value) {
        return value == null ? 0 : value;
    }


}
