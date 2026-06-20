package com.easy_buy.INVENTORY_SERVICE.client;


import com.easy_buy.COMMON_SERVICE.payload.ProductSnapshot;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

@FeignClient(
        name ="${PRODUCT_SERVICE_NAME}"
)
public interface ProductFeignClient {

       // Get Product By I'd
       @GetMapping("/api/products/{productId}")
       ProductSnapshot getProductById(
               @PathVariable("productId") UUID productId
       );
}

