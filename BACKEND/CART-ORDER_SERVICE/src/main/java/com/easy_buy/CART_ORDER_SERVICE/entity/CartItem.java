package com.easy_buy.CART_ORDER_SERVICE.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "cart_items")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CartItem extends BaseEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID cartItemId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cart_id", nullable = false)
    private Cart cart;

    @Column(nullable = false)
    private UUID productId;

    @Column(nullable = false, length = 220)
    private String productTitle;

    @Column(nullable = false)
    private String productImage;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal unitPrice;

    @Column(nullable = false)
    private Integer discountPercent = 0;

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal lineTotal;

    @PrePersist
    @PreUpdate
    public void calculateLineTotal() {

        if (unitPrice == null) {
            unitPrice = BigDecimal.ZERO;
        }

        if (quantity == null || quantity <= 0) {
            quantity = 1;
        }

        if (discountPercent == null || discountPercent < 0) {
            discountPercent = 0;
        }

        BigDecimal discountAmount = unitPrice
                .multiply(BigDecimal.valueOf(discountPercent))
                .divide(BigDecimal.valueOf(100), 4, java.math.RoundingMode.HALF_UP);

        BigDecimal finalPrice = unitPrice.subtract(discountAmount);

        lineTotal = finalPrice.multiply(BigDecimal.valueOf(quantity));
    }
}

