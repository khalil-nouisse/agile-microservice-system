package com.example.sprint_service.dto;

import java.util.UUID;

public class AssigneeLoadResponse {

    private UUID assigneeId;
    private Double estimatedLoad;
    private int workItemCount;

    public AssigneeLoadResponse(UUID assigneeId, Double estimatedLoad, int workItemCount) {
        this.assigneeId = assigneeId;
        this.estimatedLoad = estimatedLoad;
        this.workItemCount = workItemCount;
    }

    public UUID getAssigneeId() {
        return assigneeId;
    }

    public Double getEstimatedLoad() {
        return estimatedLoad;
    }

    public int getWorkItemCount() {
        return workItemCount;
    }
}
