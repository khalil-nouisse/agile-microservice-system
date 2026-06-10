package com.example.sprint_service.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

@FeignClient(name = "project-service")
public interface ProjectClient {

    @GetMapping("/api/projects/{projectId}")
    Object getProject(@PathVariable UUID projectId);
}
