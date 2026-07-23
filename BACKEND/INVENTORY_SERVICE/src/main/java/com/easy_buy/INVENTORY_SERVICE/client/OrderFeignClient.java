package com.easy_buy.INVENTORY_SERVICE.client;

import com.easy_buy.INVENTORY_SERVICE.dtos.response.OrderResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

@FeignClient(name = "${CART_ORDER_SERVICE_NAME:CART-ORDER-SERVICE}", url = "${CART_ORDER_SERVICE_URL:}")
public interface OrderFeignClient {

    @GetMapping("/api/orders/{orderId}")
    OrderResponse getOrderById(@PathVariable("orderId") UUID orderId);
}
