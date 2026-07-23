package com.easy_buy.CART_ORDER_SERVICE.payload.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ReleaseStockRequest {

    private Integer quantity;
}