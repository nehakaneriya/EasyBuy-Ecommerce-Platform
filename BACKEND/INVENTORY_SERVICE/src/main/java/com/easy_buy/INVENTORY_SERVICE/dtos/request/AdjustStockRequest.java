package com.easy_buy.INVENTORY_SERVICE.dtos.request;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdjustStockRequest {

    @NotNull(message = "Quantity delta is required")
    private Integer quantityDelta;

    private String reason;
}
