package com.easy_buy.API_GATEWAY.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;

@Component
public class AuthenticationFilter extends AbstractGatewayFilterFactory<AuthenticationFilter.Config> {


    private static final Logger log = LoggerFactory.getLogger(AuthenticationFilter.class);

    @Value("${jwt.secret:404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970}")
    private String secretKey;

    public AuthenticationFilter() {
        super(Config.class);
    }

    private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    @NotNull
    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            String path = request.getURI().getPath();
            String method = request.getMethod().name();

            log.debug("Incoming request: {} {}", method, path);

            if (isPublicEndpoint(path, method)) {
                return chain.filter(exchange);
            }

            String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return onError(exchange, "Missing or invalid Authorization header", HttpStatus.UNAUTHORIZED);
            }

            String token = authHeader.substring(7);

            try {
                Claims claims = Jwts.parser()
                        .verifyWith(getSigningKey())
                        .build()
                        .parseSignedClaims(token)
                        .getPayload();

                String tokenUserId = String.valueOf(claims.get("userId"));
                String role = String.valueOf(claims.get("role"));

                if (!isValidRole(role)) {
                    return onError(exchange, "Forbidden: Invalid user role", HttpStatus.FORBIDDEN);
                }

                if (isAdminOnlyEndpoint(path, method) && !"ADMIN".equalsIgnoreCase(role)) {
                    return onError(exchange, "Forbidden: Admin access required", HttpStatus.FORBIDDEN);
                }

                if (isUserOrGuest(role)) {
                    String targetUserId = extractUserIdFromPath(path);
                    if (targetUserId != null && !targetUserId.equalsIgnoreCase(tokenUserId)) {
                        return onError(exchange, "Forbidden: Cannot access another user's data", HttpStatus.FORBIDDEN);
                    }
                }

                ServerHttpRequest mutatedRequest = request.mutate()
                        .header("X-User-Id", tokenUserId)
                        .header("X-User-Email", claims.getSubject())
                        .header("X-User-Role", role)
                        .build();

                return chain.filter(exchange.mutate().request(mutatedRequest).build());

            } catch (Exception e) {
                log.error("JWT validation failed for path: {}", path, e);
                return onError(exchange, "Unauthorized: Invalid or expired token", HttpStatus.UNAUTHORIZED);
            }
        };
    }

    private boolean isPublicEndpoint(String path, String method) {
        return path.contains("/public/") ||
                path.contains("/api/auth/login") ||
                path.contains("/api/auth/refresh") ||
                (path.contains("/api/auth/register") && "POST".equalsIgnoreCase(method)) ||
                (path.contains("/api/products") && "GET".equalsIgnoreCase(method)) ||
                (path.contains("/api/categories") && "GET".equalsIgnoreCase(method)) ||
                (path.contains("/api/reviews") && "GET".equalsIgnoreCase(method));
    }

    private boolean isValidRole(String role) {
        return "ADMIN".equalsIgnoreCase(role) ||
                "USER".equalsIgnoreCase(role) ||
                "GUEST".equalsIgnoreCase(role);
    }

    private boolean isUserOrGuest(String role) {
        return "USER".equalsIgnoreCase(role) || "GUEST".equalsIgnoreCase(role);
    }

    private boolean isAdminOnlyEndpoint(String path, String method) {
        if (path.contains("/api/users/change-role")) return true;

        if (path.contains("/api/users") && "GET".equalsIgnoreCase(method)
                && !path.matches(".*/api/users/[a-fA-F0-9-]+")) return true;

        if ((path.contains("/api/products") || path.contains("/api/categories")
                || path.contains("/api/reviews")) && !"GET".equalsIgnoreCase(method)) return true;

        if (path.contains("/api/inventories") && !"GET".equalsIgnoreCase(method)) return true;

        return false;
    }

    private String extractUserIdFromPath(String path) {
        String[] prefixes = {"/api/carts/", "/api/orders/user/", "/api/orders/", "/api/users/"};

        for (String prefix : prefixes) {
            int index = path.indexOf(prefix);
            if (index != -1) {
                String sub = path.substring(index + prefix.length());

                if (sub.endsWith("/checkout")) {
                    sub = sub.replace("/checkout", "");
                }

                int slashIndex = sub.indexOf("/");
                String extractedId = (slashIndex != -1) ? sub.substring(0, slashIndex) : sub;

                if (extractedId.equals("login") || extractedId.equals("refresh")
                        || extractedId.equals("change-role")) {
                    continue;
                }
                return extractedId;
            }
        }
        return null;
    }

    private Mono<Void> onError(ServerWebExchange exchange, String err, HttpStatus httpStatus) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(httpStatus);
        return response.setComplete();
    }

    public static class Config {
    }
}
