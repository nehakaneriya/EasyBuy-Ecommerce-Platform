package com.easy_buy.CART_ORDER_SERVICE.payload.response;

import com.easy_buy.CART_ORDER_SERVICE.entity.OrderStatus;
import com.easy_buy.CART_ORDER_SERVICE.entity.PaymentMethod;
import com.easy_buy.CART_ORDER_SERVICE.entity.PaymentStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderResponse {

    private UUID orderId;

    private String billingName;

    private String billingPhone;

    private PaymentStatus paymentStatus;

    private String extraInformation;

    private String orderNumber;

    private UUID userId;

    private String shippingAddress;

    private PaymentMethod paymentMethod;

    private OrderStatus orderStatus;

    private BigDecimal totalAmount;

    private Instant createdAt;

    private Instant updatedAt;

    private Instant cancelledAt;

    private List<OrderItemResponse> orderItems;
}