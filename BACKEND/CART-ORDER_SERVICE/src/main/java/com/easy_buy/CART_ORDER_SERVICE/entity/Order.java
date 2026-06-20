package com.easy_buy.CART_ORDER_SERVICE.entity;



import jakarta.persistence.*;
import lombok.*;


import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "orders")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Order extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID orderId;

    @Column(nullable = false)
    private UUID userId;

    @Column(nullable = false, unique = true, length = 36)
    private String orderNumber;

    @Column(nullable = false, length = 120)
    private String billingName;

    @Column(nullable = false, length = 12)
    private String billingPhone;

    @Column(nullable = false, length = 400)
    private String shippingAddress;


    @Column(length = 80)
    @Enumerated(EnumType.STRING)
    private PaymentMethod paymentMethod;

    @Enumerated(EnumType.STRING)
    private PaymentStatus paymentStatus;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private OrderStatus orderStatus;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal totalAmount = BigDecimal.ZERO;

    @OneToMany(
            mappedBy = "order",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    private List<OrderItem> orderItems = new ArrayList<>();

    @Column
    private Instant cancelledAt;

    @Column(columnDefinition = "TEXT")
    private String extraInformation;

}
