package com.easy_buy.PAYMENT_SERVICE.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "payments")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Payment extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID paymentId;

    @Column(nullable = false)
    private UUID orderId;

    @Column(nullable = false)
    private UUID userId;

    @Column(nullable = false, unique = true, length = 36)
    private String paymentNumber;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private PaymentStatus paymentStatus;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private PaymentMethod paymentMethod;

    // Gateway transaction ID (e.g. Razorpay/Stripe txn ID) — null for OFFLINE_PAYMENT
    @Column(length = 100)
    private String transactionId;

    // Timestamp when payment was successfully completed
    @Column
    private Instant paidAt;

    // Timestamp when payment was refunded (if applicable)
    @Column
    private Instant refundedAt;

    // Optional notes / failure reason
    @Column(columnDefinition = "TEXT")
    private String remarks;
}
