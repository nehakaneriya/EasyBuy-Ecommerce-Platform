package com.easy_buy.COMMON_SERVICE.events;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.UUID;

@Getter
@Setter
@ToString
public class PaymentEvent {

    private UUID orderId;

    private UUID paymentId;

    // COMPLETED / FAILED / REFUNDED
    private String paymentStatus;

    private String message;
}
