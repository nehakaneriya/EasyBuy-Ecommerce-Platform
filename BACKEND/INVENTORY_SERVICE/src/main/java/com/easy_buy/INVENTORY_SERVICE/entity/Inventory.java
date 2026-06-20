package com.easy_buy.INVENTORY_SERVICE.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.UUID;
import java.time.Instant;


@Entity
@Table(
        name = "inventories",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_inventory_sku", columnNames = "sku"),
                @UniqueConstraint(name = "uk_inventory_product_id", columnNames = "product_id")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Inventory {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "product_id", nullable = false, unique = true, updatable = false)
    private UUID productId;

    @Column(nullable = false, unique = true, length = 128)
    private String sku;

    @Column(nullable = false, length = 200)
    private String productName;

    @Column(nullable = false, length = 120)
    private String warehouseLocation;

    @Column(nullable = false)
    private Integer availableQuantity;

    @Column(nullable = false)
    private Integer reservedQuantity;

    @Column(nullable = false)
    private Integer reorderLevel;

    @Column(nullable = false)
    private Boolean active;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    private String reasonForAdjustment;


    @PrePersist
    public void onCreate() {

        Instant now = Instant.now();

        this.createdAt = now;
        this.updatedAt = now;

        if (this.availableQuantity == null) {
            this.availableQuantity = 0;
        }

        if (this.reservedQuantity == null) {
            this.reservedQuantity = 0;
        }

        if (this.reorderLevel == null) {
            this.reorderLevel = 0;
        }

        if (this.active == null) {
            this.active = true;
        }
    }

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = Instant.now();
    }


    // =========================
    // BUSINESS METHODS
    // =========================

    public int getTotalQuantity() {
        return safeInt(this.availableQuantity) + safeInt(this.reservedQuantity);
    }

    public boolean isLowStock() {
        return safeInt(this.availableQuantity) <= safeInt(this.reorderLevel);
    }

    public boolean isInStock() {
        return safeInt(this.availableQuantity) > 0;
    }

    private int safeInt(Integer value) {
        return value == null ? 0 : value;
    }
}