package com.example.sprint_service.dto;

import java.util.UUID;

public class AssigneeLoadResponse {
    private UUID assigneeId;
    private Double totalEstimation;
    private Integer workItemCount;

    public AssigneeLoadResponse(UUID assigneeId, Double totalEstimation, Integer workItemCount) {
        this.assigneeId = assigneeId;
        this.totalEstimation = totalEstimation;
        this.workItemCount = workItemCount;
    }

    public UUID getAssigneeId() {
        return assigneeId;
    }

    public Double getTotalEstimation() {
        return totalEstimation;
    }

    public Integer getWorkItemCount() {
        return workItemCount;
    }
}
