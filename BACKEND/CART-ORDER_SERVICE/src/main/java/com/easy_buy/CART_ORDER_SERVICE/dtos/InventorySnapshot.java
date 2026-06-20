package com.easy_buy.CART_ORDER_SERVICE.dtos;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class InventorySnapshot {

    private UUID inventoryId;

    private UUID productId;

    private String sku;

    private Integer availableQuantity;

    private Integer reservedQuantity;

    private Boolean active;
}