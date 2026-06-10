package com.example.sprint_service.dto;

import com.example.sprint_service.model.Sprint;
import com.example.sprint_service.model.SprintStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class SprintResponse {

    private UUID id;
    private String name;
    private String goal;
    private UUID projectId;
    private LocalDate startDate;
    private LocalDate endDate;
    private Double plannedCapacity;
    private SprintStatus status;
    private SprintCapacityResponse capacity;
    private List<SprintWorkItemResponse> workItems;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static SprintResponse from(
            Sprint sprint,
            SprintCapacityResponse capacity,
            List<SprintWorkItemResponse> workItems
    ) {
        SprintResponse response = new SprintResponse();
        response.id = sprint.getId();
        response.name = sprint.getName();
        response.goal = sprint.getGoal();
        response.projectId = sprint.getProjectId();
        response.startDate = sprint.getStartDate();
        response.endDate = sprint.getEndDate();
        response.plannedCapacity = sprint.getPlannedCapacity();
        response.status = sprint.getStatus();
        response.capacity = capacity;
        response.workItems = workItems;
        response.createdAt = sprint.getCreatedAt();
        response.updatedAt = sprint.getUpdatedAt();
        return response;
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getGoal() {
        return goal;
    }

    public UUID getProjectId() {
        return projectId;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public Double getPlannedCapacity() {
        return plannedCapacity;
    }

    public SprintStatus getStatus() {
        return status;
    }

    public SprintCapacityResponse getCapacity() {
        return capacity;
    }

    public List<SprintWorkItemResponse> getWorkItems() {
        return workItems;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
