package com.easy_buy.INVENTORY_SERVICE.repository;


import com.easy_buy.INVENTORY_SERVICE.entity.Inventory;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface InventoryRepository extends JpaRepository<Inventory, UUID> {


    Optional<Inventory> findBySku(String sku);
    Optional<Inventory> findByProductId(UUID productId);

    //find inventory by id with pessimistic lock
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select i from Inventory i where i.id = :id")
    Optional<Inventory> findByIdForUpdate(@Param("id") UUID id);

    //find inventory by productid
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select i from Inventory i where i.productId = :productId")
    Optional<Inventory> findByProductIdForUpdate(@Param("productId") UUID productId);

    boolean existsBySku(String sku);
    boolean existsByProductId(UUID productId);

    List<Inventory> findByActiveTrueOrderByProductNameAsc();

    // Find all active inventories where available quantity is less than or equal to a specified threshold, ordered by available quantity ascending
    List<Inventory> findByAvailableQuantityLessThanEqualAndActiveTrueOrderByAvailableQuantityAsc(int threshold
    );
}