package com.example.project_service.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.UUID;

@FeignClient(name = "auth-service")
public interface AuthServiceClient {

    @GetMapping("/api/auth/users/by-email")
    UserSummary getUserByEmail(@RequestParam("email") String email);

    record UserSummary(UUID id, String firstName, String lastName, String email) {}
}
