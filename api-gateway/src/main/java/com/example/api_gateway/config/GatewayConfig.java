package com.example.api_gateway.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayConfig {

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("auth-service", r -> r
                        .path("/api/auth/**")
                        .uri("lb://auth-service"))
                .route("project-service", r -> r
                        .path("/api/projects/**")
                        .uri("lb://project-service"))
                .route("workitem-service", r -> r
                        .path("/api/workitems/**")
                        .uri("lb://workitem-service"))
                .route("sprint-service", r -> r
                        .path("/api/sprints/**")
                        .uri("lb://sprint-service"))
                .route("notification-service", r -> r
                        .path("/api/notifications/**")
                        .uri("lb://notification-service"))
                .build();
    }
}