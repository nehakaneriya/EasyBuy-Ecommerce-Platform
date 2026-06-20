package com.easy_buy.CART_ORDER_SERVICE.entity;


import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "carts")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Cart extends BaseEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID cartId;

    @Column(nullable = false)
    private UUID userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CartStatus cartStatus = CartStatus.ACTIVE;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal cartTotal = BigDecimal.ZERO;

    @Column
    private Instant checkedOutAt;

    @OneToMany(
            mappedBy = "cart",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    private List<CartItem> cartItems = new ArrayList<>();

    @PrePersist
    @PreUpdate
    public void calculateCartTotal() {

        if (cartItems == null || cartItems.isEmpty()) {
            cartTotal = BigDecimal.ZERO;
            return;
        }

        cartTotal = cartItems.stream()
                .map(CartItem::getLineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
