package com.easy_buy.INVENTORY_SERVICE.dtos.response;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItemResponse {

    private UUID productId;

    private String productTitle;

    private Integer quantity;
}
