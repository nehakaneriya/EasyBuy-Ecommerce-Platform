package com.easy_buy.CART_ORDER_SERVICE.payload.response;

import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CartItemResponse {

    private UUID cartItemId;

    private UUID productId;

    private String productTitle;

    private String productImage;

    private BigDecimal unitPrice;

    private Integer discountPercent;

    private Integer quantity;

    private BigDecimal lineTotal;
}