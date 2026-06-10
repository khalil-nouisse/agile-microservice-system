package com.example.sprint_service.dto;

import com.example.sprint_service.model.SprintWorkItem;

import java.time.LocalDateTime;
import java.util.UUID;

public class SprintWorkItemResponse {

    private UUID id;
    private UUID sprintId;
    private UUID workItemId;
    private String title;
    private String status;
    private Double estimation;
    private UUID assigneeId;
    private LocalDateTime addedAt;

    public static SprintWorkItemResponse from(SprintWorkItem item) {
        SprintWorkItemResponse response = new SprintWorkItemResponse();
        response.id = item.getId();
        response.sprintId = item.getSprint().getId();
        response.workItemId = item.getWorkItemId();
        response.title = item.getTitle();
        response.status = item.getStatus();
        response.estimation = item.getEstimation();
        response.assigneeId = item.getAssigneeId();
        response.addedAt = item.getAddedAt();
        return response;
    }

    public UUID getId() {
        return id;
    }

    public UUID getSprintId() {
        return sprintId;
    }

    public UUID getWorkItemId() {
        return workItemId;
    }

    public String getTitle() {
        return title;
    }

    public String getStatus() {
        return status;
    }

    public Double getEstimation() {
        return estimation;
    }

    public UUID getAssigneeId() {
        return assigneeId;
    }

    public LocalDateTime getAddedAt() {
        return addedAt;
    }
}
