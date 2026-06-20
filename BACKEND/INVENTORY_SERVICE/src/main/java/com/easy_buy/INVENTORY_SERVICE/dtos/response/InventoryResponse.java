package com.easy_buy.INVENTORY_SERVICE.dtos.response;

import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryResponse {

    private UUID id;

    private UUID productId;

    private String sku;

    private String productName;

    private String warehouseLocation;

    private Integer availableQuantity;

    private Integer reservedQuantity;

    private Integer reorderLevel;

    private Integer totalQuantity;

    private Boolean lowStock;

    private Boolean inStock;

    private Boolean active;

    private Instant createdAt;

    private Instant updatedAt;
}