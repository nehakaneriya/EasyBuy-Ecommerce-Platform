package com.easy_buy.CART_ORDER_SERVICE.client.fallbacks;

import com.easy_buy.CART_ORDER_SERVICE.client.ProductFeignClient;
import com.easy_buy.COMMON_SERVICE.payload.ProductSnapshot;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@Slf4j
public class ProductClientFallback implements ProductFeignClient {

    @Override
    public ProductSnapshot getProductById(UUID productId) {
        log.info("ProductClientFallback: getProductById called with productId: {}", productId);
        return null;
    }
}
