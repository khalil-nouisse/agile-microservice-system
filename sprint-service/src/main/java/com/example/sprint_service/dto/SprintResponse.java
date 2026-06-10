package com.example.sprint_service.dto;

import com.example.sprint_service.model.Sprint;
import com.example.sprint_service.model.SprintStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public class SprintResponse {
    private UUID id;
    private UUID projectId;
    private String name;
    private String goal;
    private LocalDate startDate;
    private LocalDate endDate;
    private Double capacity;
    private SprintStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static SprintResponse from(Sprint sprint) {
        SprintResponse response = new SprintResponse();
        response.id = sprint.getId();
        response.projectId = sprint.getProjectId();
        response.name = sprint.getName();
        response.goal = sprint.getGoal();
        response.startDate = sprint.getStartDate();
        response.endDate = sprint.getEndDate();
        response.capacity = sprint.getCapacity();
        response.status = sprint.getStatus();
        response.createdAt = sprint.getCreatedAt();
        response.updatedAt = sprint.getUpdatedAt();
        return response;
    }

    public UUID getId() {
        return id;
    }

    public UUID getProjectId() {
        return projectId;
    }

    public String getName() {
        return name;
    }

    public String getGoal() {
        return goal;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public Double getCapacity() {
        return capacity;
    }

    public SprintStatus getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
