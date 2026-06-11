package com.example.workitem_service.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

@FeignClient(name = "project-service")
public interface ProjectClient {

    @GetMapping("/api/projects/{projectId}")
    ProjectSummary getProject(@PathVariable UUID projectId);

    record ProjectSummary(UUID id, String name, UUID ownerId) {}
}
