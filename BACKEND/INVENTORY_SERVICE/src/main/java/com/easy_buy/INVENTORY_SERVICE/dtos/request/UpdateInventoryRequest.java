package com.easy_buy.INVENTORY_SERVICE.dtos.request;

import jakarta.validation.constraints.Min;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateInventoryRequest {

    private String productName;

    private String warehouseLocation;

    @Min(value = 0, message = "Reorder level cannot be negative")
    private Integer reorderLevel;

    private Boolean active;
}