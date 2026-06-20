package com.easy_buy.INVENTORY_SERVICE.service;

import com.easy_buy.INVENTORY_SERVICE.dtos.request.*;
import com.easy_buy.INVENTORY_SERVICE.dtos.response.InventoryResponse;

import java.util.List;
import java.util.UUID;

public interface InventoryService {

    InventoryResponse create(CreateInventoryRequest request);

    InventoryResponse update(UUID id,UpdateInventoryRequest request);

    InventoryResponse getById(UUID id);

    InventoryResponse getBySku(String sku);

    InventoryResponse getByProductId(UUID productId);

    List<InventoryResponse> getAll();

    List<InventoryResponse> getLowStock(int threshold);

    InventoryResponse adjustStock(UUID id,AdjustStockRequest request);

    InventoryResponse reserveStock(UUID id,ReserveStockRequest request);

    InventoryResponse releaseStock(UUID id,ReleaseStockRequest request);

    InventoryResponse reserveStockByProductId(UUID productId,ReserveStockRequest request);

    InventoryResponse releaseStockByProductId(UUID productId,ReleaseStockRequest request);

    void delete(UUID id);
}
