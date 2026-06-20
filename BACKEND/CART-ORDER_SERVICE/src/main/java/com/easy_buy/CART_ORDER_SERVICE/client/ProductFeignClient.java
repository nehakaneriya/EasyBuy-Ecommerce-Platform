package com.easy_buy.CART_ORDER_SERVICE.client;

import com.easy_buy.CART_ORDER_SERVICE.client.fallbacks.ProductClientFallback;
import com.easy_buy.COMMON_SERVICE.payload.ProductSnapshot;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

@FeignClient(
        name ="${PRODUCT_SERVICE_NAME}",
        fallback = ProductClientFallback.class
)
public interface ProductFeignClient {

    // Get Product By I'd
    @GetMapping("/api/products/{productId}")
    ProductSnapshot getProductById(
            @PathVariable("productId") UUID productId
    );
}
