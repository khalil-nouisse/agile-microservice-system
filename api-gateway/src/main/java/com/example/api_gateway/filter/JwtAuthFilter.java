package com.example.api_gateway.filter;

import com.example.api_gateway.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
public class JwtAuthFilter implements GlobalFilter, Ordered {

    @Autowired
    private JwtUtil jwtUtil;

    private static final List<String> PUBLIC_ENDPOINTS = List.of(
            "/api/auth/login",
            "/api/auth/register"
    );

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();

        // Skip JWT check for public endpoints
        if (isPublicEndpoint(path)) {
            return chain.filter(exchange);
        }

        // Get Authorization header
        String authHeader = exchange.getRequest()
                .getHeaders()
                .getFirst("Authorization");

        // Check header exists and starts with Bearer
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        String token = authHeader.substring(7);

        // Validate token
        if (!jwtUtil.isTokenValid(token)) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        String userRole = jwtUtil.extractRole(token);

        // Inject user info into headers for downstream services
        ServerWebExchange modifiedExchange = exchange.mutate()
                .request(r -> {
                    r.header("X-User-Id", jwtUtil.extractUserId(token));
                    if (userRole != null) {
                        r.header("X-User-Role", userRole);
                    }
                })
                .build();

        return chain.filter(modifiedExchange);
    }

    private boolean isPublicEndpoint(String path) {
        return PUBLIC_ENDPOINTS.stream()
                .anyMatch(path::startsWith);
    }

    @Override
    public int getOrder() {
        return -1;
    }
}
