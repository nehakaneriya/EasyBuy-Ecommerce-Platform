package com.easy_buy.COMMON_SERVICE.events;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;
import java.util.UUID;



@Getter
@Setter
@ToString
public class OrderEvent {

    private UUID orderId;
    private UUID userId;
    private String status;
    private String message;
    private BigDecimal totalAmount;
}
