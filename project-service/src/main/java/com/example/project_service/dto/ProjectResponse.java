package com.example.project_service.dto;

import com.example.project_service.model.AgileMethodology;
import com.example.project_service.model.Project;
import com.example.project_service.model.ProjectStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public class ProjectResponse {

    private UUID id;
    private String name;
    private String description;
    private AgileMethodology methodology;
    private ProjectStatus status;
    private UUID ownerId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static ProjectResponse from(Project project) {
        ProjectResponse response = new ProjectResponse();
        response.id = project.getId();
        response.name = project.getName();
        response.description = project.getDescription();
        response.methodology = project.getMethodology();
        response.status = project.getStatus();
        response.ownerId = project.getOwnerId();
        response.createdAt = project.getCreatedAt();
        response.updatedAt = project.getUpdatedAt();
        return response;
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public AgileMethodology getMethodology() {
        return methodology;
    }

    public ProjectStatus getStatus() {
        return status;
    }

    public UUID getOwnerId() {
        return ownerId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
