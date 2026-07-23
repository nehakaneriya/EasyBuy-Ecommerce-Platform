package com.easy_buy.INVENTORY_SERVICE.dtos.response;

import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderResponse {

    private UUID orderId;

    private String orderNumber;

    private UUID userId;

    private String billingName;

    private String billingPhone;

    private String shippingAddress;

    private String paymentStatus;

    private String paymentMethod;

    private String orderStatus;

    private BigDecimal totalAmount;

    private Instant createdAt;

    private List<OrderItemResponse> orderItems;
}
