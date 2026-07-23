package com.easy_buy.CART_ORDER_SERVICE.payload.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class ReserveStockRequest {

    private Integer quantity;
}