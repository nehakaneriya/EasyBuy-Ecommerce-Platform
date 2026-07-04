package com.easy_buy.PAYMENT_SERVICE.dto.response;

import com.easy_buy.PAYMENT_SERVICE.entity.PaymentMethod;
import com.easy_buy.PAYMENT_SERVICE.entity.PaymentStatus;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PaymentResponse {

    private UUID paymentId;

    private UUID orderId;

    private UUID userId;

    private String paymentNumber;

    private BigDecimal amount;

    private PaymentStatus paymentStatus;

    private PaymentMethod paymentMethod;

    // Present only for ONLINE_PAYMENT
    private String transactionId;

    private String remarks;

    private Instant paidAt;

    private Instant refundedAt;

    private Instant createdAt;

    private Instant updatedAt;
}
