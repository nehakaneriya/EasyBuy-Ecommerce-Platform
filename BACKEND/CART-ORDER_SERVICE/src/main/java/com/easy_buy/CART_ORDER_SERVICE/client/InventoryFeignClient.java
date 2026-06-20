package com.easy_buy.CART_ORDER_SERVICE.client;

import com.easy_buy.CART_ORDER_SERVICE.dtos.InventorySnapshot;
import com.easy_buy.CART_ORDER_SERVICE.payload.request.ReleaseStockRequest;
import com.easy_buy.CART_ORDER_SERVICE.payload.request.ReserveStockRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.UUID;

@FeignClient(
        name ="${INVENTORY_SERVICE_NAME}"
)
public interface InventoryFeignClient {

    // Get Inventory By Product I'd
    @GetMapping("/api/inventories/product/{productId}")
    InventorySnapshot getByProductId(
            @PathVariable UUID productId
    );

    // Reserve Stock
    @PatchMapping("/api/inventories/product/{productId}/reserve")
    InventorySnapshot reserveStock(
            @PathVariable UUID productId,
            @RequestBody ReserveStockRequest request
    );

    // Release Stock
    @PatchMapping("/api/inventories/product/{productId}/release")
    InventorySnapshot releaseStock(
            @PathVariable UUID productId,
            @RequestBody ReleaseStockRequest request
    );
}
