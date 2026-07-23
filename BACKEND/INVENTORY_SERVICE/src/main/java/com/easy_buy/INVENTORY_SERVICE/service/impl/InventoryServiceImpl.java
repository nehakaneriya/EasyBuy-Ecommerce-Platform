package com.easy_buy.INVENTORY_SERVICE.service.impl;

import com.easy_buy.COMMON_SERVICE.payload.ProductSnapshot;
import com.easy_buy.INVENTORY_SERVICE.client.ProductFeignClient;
import com.easy_buy.INVENTORY_SERVICE.dtos.request.*;
import com.easy_buy.INVENTORY_SERVICE.dtos.response.InventoryResponse;
import com.easy_buy.INVENTORY_SERVICE.entity.Inventory;
import com.easy_buy.INVENTORY_SERVICE.exception.BusinessRuleException;
import com.easy_buy.INVENTORY_SERVICE.exception.InsufficientStockException;
import com.easy_buy.INVENTORY_SERVICE.exception.ResourceNotFoundException;
import com.easy_buy.INVENTORY_SERVICE.repository.InventoryRepository;
import com.easy_buy.INVENTORY_SERVICE.service.InventoryService;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class InventoryServiceImpl implements InventoryService {

    private final InventoryRepository repository;
    private final ProductFeignClient productClient;

    // Create Inventory
    @Override
    public InventoryResponse create(CreateInventoryRequest request) {

        ProductSnapshot productSnapshot=null;
        try{
            productSnapshot = productClient.getProductById(request.getProductId());
        }
        catch (FeignException e){
            throw new BusinessRuleException("Unable to contact Product Service: " + e.getMessage());
        }

        if (productSnapshot == null) {
            throw new BusinessRuleException("Product not found for productId: " + request.getProductId());
        }

        String sku = normalizeSku(request.getSku());


        if (repository.existsBySku(sku)) {
            throw new BusinessRuleException("Inventory already exists for sku: " + sku);
        }
        // Check if inventory already exists for the given productId to prevent duplicates
        if (repository.existsByProductId(request.getProductId())) {
            throw new BusinessRuleException("Inventory already exists for productId: " + request.getProductId());
        }

        String productName = StringUtils.hasText(request.getProductName())
                ? trim(request.getProductName())
                : trim(productSnapshot.getTitle());

        Inventory savedInventory = repository.save(
                Inventory.builder()
                        .productId(request.getProductId())
                        .sku(sku)
                        .productName(productName)
                        .warehouseLocation(trim(request.getWarehouseLocation()))
                        .availableQuantity(defaultZero(request.getAvailableQuantity()))
                        .reservedQuantity(defaultZero(request.getReservedQuantity()))
                        .reorderLevel(defaultZero(request.getReorderLevel()))
                        .active(request.getActive() == null || request.getActive())
                        .build()
        );
        return toResponse(savedInventory);
    }

    // Update Inventory
    @Override
    public InventoryResponse update(UUID id, UpdateInventoryRequest request) {
        Inventory inventory = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory not found for id: " + id));

        if (StringUtils.hasText(request.getProductName())) {
            inventory.setProductName(trim(request.getProductName()));
        }
        if (StringUtils.hasText(request.getWarehouseLocation())) {
            inventory.setWarehouseLocation(trim(request.getWarehouseLocation()));
        }
        if (request.getReorderLevel() != null) {
            inventory.setReorderLevel(request.getReorderLevel());
        }
        if (request.getActive() != null) {
            inventory.setActive(request.getActive());
        }

        Inventory updatedInventory = repository.save(inventory);
        return toResponse(updatedInventory);
    }

    // Get By I'd
    @Override
    @Transactional(readOnly = true)
    public InventoryResponse getById(UUID id) {
        Inventory inventory = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory not found for id: " + id));
        return toResponse(inventory);
    }

    // Get By SKU
    @Override
    @Transactional(readOnly = true)
    public InventoryResponse getBySku(String sku) {
        Inventory inventory = repository.findBySku(normalizeSku(sku))
                .orElseThrow(() -> new ResourceNotFoundException("Inventory not found for sku: " + sku));
        return toResponse(inventory);
    }

    // Get By Product Id
    @Override
    @Transactional(readOnly = true)
    public InventoryResponse getByProductId(UUID productId) {
        Inventory inventory = repository.findByProductId(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory not found for productId: " + productId));
        return toResponse(inventory);
    }

    // Get All
    @Override
    @Transactional(readOnly = true)
    public List<InventoryResponse> getAll() {
        return repository.findByActiveTrueOrderByProductNameAsc()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    // Get Low Stock
    @Override
    @Transactional(readOnly = true) // Read-only transaction for better performance on read operations
    public List<InventoryResponse> getLowStock(int threshold) {
        return repository.findByAvailableQuantityLessThanEqualAndActiveTrueOrderByAvailableQuantityAsc(threshold)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    // Adjust Stock
    @Override
    public InventoryResponse adjustStock(UUID id, AdjustStockRequest request) {
        Inventory inventory = repository.findByIdForUpdate(id)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory not found for id: " + id));

        int updatedQuantity = inventory.getAvailableQuantity() + request.getQuantityDelta();
        if (updatedQuantity < 0) {
            throw new BusinessRuleException("Available quantity cannot be negative");
        }
        inventory.setAvailableQuantity(updatedQuantity);
        inventory.setReasonForAdjustment(trim(request.getReason()));
        Inventory updatedInventory = repository.save(inventory);
        return toResponse(updatedInventory);
    }

    // Reserve Stock
    @Override
    public InventoryResponse reserveStock(UUID id, ReserveStockRequest request) {
        Inventory inventory = repository.findByIdForUpdate(id)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory not found for id: " + id));
        return reserveStock(inventory, request.getQuantity());
    }

    // Release Stock
    @Override
    public InventoryResponse releaseStock(UUID id, ReleaseStockRequest request) {
        Inventory inventory = repository.findByIdForUpdate(id)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory not found for id: " + id));
        return releaseStock(inventory, request.getQuantity());
    }

    // Reserve Stock By Product Id
    @Override
    public InventoryResponse reserveStockByProductId(UUID productId, ReserveStockRequest request) {
        Inventory inventory = repository.findByProductIdForUpdate(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory not found for productId: " + productId));
        return reserveStock(inventory, request.getQuantity());
    }

    // Release Stock By Product Id
    @Override
    public InventoryResponse releaseStockByProductId(UUID productId, ReleaseStockRequest request) {
        Inventory inventory = repository.findByProductIdForUpdate(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory not found for productId: " + productId));
        return releaseStock(inventory, request.getQuantity());
    }

    // Delete Inventory
    @Override
    public void delete(UUID id) {
        Inventory inventory = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory not found for id: " + id));
        repository.delete(inventory);
    }

    // Helper Methods
    // Normalize SKU by trimming and converting to uppercase
    private String normalizeSku(String sku) {
        if (!StringUtils.hasText(sku)) {
            throw new BusinessRuleException("SKU is required");
        }
        return sku.trim().toUpperCase();
    }

    private String trim(String value) {
        return value == null ? null : value.trim();
    }

    private int defaultZero(Integer value) {
        return value == null ? 0 : value;
    }

    // Entity -> Response DTO
    private InventoryResponse toResponse(Inventory inventory) {
        return InventoryResponse.builder()
                .id(inventory.getId())
                .productId(inventory.getProductId())
                .sku(inventory.getSku())
                .productName(inventory.getProductName())
                .warehouseLocation(inventory.getWarehouseLocation())
                .availableQuantity(inventory.getAvailableQuantity())
                .reservedQuantity(inventory.getReservedQuantity())
                .reorderLevel(inventory.getReorderLevel())
                .totalQuantity(inventory.getTotalQuantity())
                .lowStock(inventory.isLowStock())
                .inStock(inventory.isInStock())
                .active(inventory.getActive())
                .createdAt(inventory.getCreatedAt())
                .updatedAt(inventory.getUpdatedAt())
                .build();
    }

    // Common Reserve Logic
    private InventoryResponse reserveStock(Inventory inventory, int quantity) {
        if (inventory.getAvailableQuantity() < quantity) {
            throw new InsufficientStockException("Insufficient stock available");
        }
        inventory.setAvailableQuantity(inventory.getAvailableQuantity() - quantity);
        inventory.setReservedQuantity(inventory.getReservedQuantity() + quantity);

        inventory.setReasonForAdjustment("Reserved " + quantity + " items");
        Inventory updatedInventory = repository.save(inventory);
        return toResponse(updatedInventory);
    }

    // Common Release Logic
    private InventoryResponse releaseStock(Inventory inventory, int quantity) {
        if (inventory.getReservedQuantity() < quantity) {
            throw new InsufficientStockException("Reserved quantity is insufficient");
        }
        inventory.setReservedQuantity(inventory.getReservedQuantity() - quantity);
        inventory.setAvailableQuantity(inventory.getAvailableQuantity() + quantity);

        inventory.setReasonForAdjustment("Released " + quantity + " items");
        Inventory updatedInventory = repository.save(inventory);
        return toResponse(updatedInventory);
    }
}