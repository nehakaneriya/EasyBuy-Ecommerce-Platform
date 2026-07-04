package com.easy_buy.API_GATEWAY.config;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Configuration
public class RouteConfig {


    private final String productServiceID;
    private final String CartOrderServiceID;
    private final String userServiceID;
    private final AuthenticationFilter authFilter;

    public RouteConfig(@Value("${product.service.id}") String productServiceID,
                       @Value("${cart.order.service.id}") String CartOrderServiceID,
                       @Value("${user.service.id}") String userServiceID,
                       AuthenticationFilter authFilter) {
        this.productServiceID = productServiceID;
        this.CartOrderServiceID = CartOrderServiceID;
        this.userServiceID = userServiceID;
        this.authFilter = authFilter;
    }

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {

        return builder.routes()

                .route("product-service", r -> r
                        .path("/product-service/**")
                        .filters(f -> f
                                .filter(authFilter.apply(new AuthenticationFilter.Config()))
                                .stripPrefix(1)
                                .requestRateLimiter(rateLimiterConfig -> rateLimiterConfig.setKeyResolver(KeyResolver())
                                        .setRateLimiter(redisRateLimiter())
                                )
                                .circuitBreaker(config -> config.setName("productCircuitBreaker")
                                        .setFallbackUri("forward:/product-fallback")))
                        .uri(productServiceID))

                .route("cart-order-service", r -> r
                        .path("/cart-order-service/**")
                        .filters(f -> f
                                .filter(authFilter.apply(new AuthenticationFilter.Config()))
                                .stripPrefix(1)

                                .retry(retryConfig ->
                                        retryConfig
                                                .setRetries(3)
                                                .setMethods(HttpMethod.GET)
                                                .setBackoff(Duration.ofMillis(100),
                                                            Duration.ofSeconds(2),
                                                        2,
                                                        true)

                                ))

                        .uri(CartOrderServiceID))

                .route("user-service", r -> r
                        .path("/user-service/**")
                        .filters(f -> f
                                .filter(authFilter.apply(new AuthenticationFilter.Config()))
                                .stripPrefix(1))
                        .uri(userServiceID))

                .build();
    }

    @Bean
    public KeyResolver KeyResolver() {
        return exchange -> Mono.just(exchange.getRequest().getHeaders().getFirst("user"));
    }

    @Bean
    public RedisRateLimiter redisRateLimiter() {
        return new RedisRateLimiter(1, 1, 1);
    }
}
