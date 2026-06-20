package com.easy_buy.API_GATEWAY.controller;


import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RequestMapping
@RestController
public class FallBackController {

    @RequestMapping("/product-fallback")
    public Mono<String> productServiceFallback() {
        return Mono.just("Product Service is currently unavailable. Please try again later.");
    }
}
