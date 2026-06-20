package com.easy_buy.CART_ORDER_SERVICE.payload.response;

import com.easy_buy.CART_ORDER_SERVICE.entity.CartStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CartResponse {

    private UUID cartId;

    private UUID userId;

    private CartStatus cartStatus;

    private BigDecimal cartTotal;

    private Instant createdAt;

    private Instant updatedAt;

    private List<CartItemResponse> cartItems;
}
