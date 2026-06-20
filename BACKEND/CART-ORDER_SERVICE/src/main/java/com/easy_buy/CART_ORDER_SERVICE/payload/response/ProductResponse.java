package com.easy_buy.CART_ORDER_SERVICE.payload.response;

import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProductResponse {

    private UUID id;

    private String name;

    private String description;

    private BigDecimal price;

    private String imageUrl;

    private Boolean active;
}