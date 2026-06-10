package com.example.sprint_service.dto;

import java.util.List;
import java.util.UUID;

public class SprintCapacityResponse {
    private UUID sprintId;
    private Double capacity;
    private Double committed;
    private Double remaining;
    private Double utilizationPercent;
    private Integer workItemCount;
    private List<AssigneeLoadResponse> assigneeLoads;

    public SprintCapacityResponse(
            UUID sprintId,
            Double capacity,
            Double committed,
            Integer workItemCount,
            List<AssigneeLoadResponse> assigneeLoads
    ) {
        this.sprintId = sprintId;
        this.capacity = capacity;
        this.committed = committed;
        this.remaining = capacity - committed;
        this.utilizationPercent = capacity == 0 ? 0 : (committed / capacity) * 100;
        this.workItemCount = workItemCount;
        this.assigneeLoads = assigneeLoads;
    }

    public UUID getSprintId() {
        return sprintId;
    }

    public Double getCapacity() {
        return capacity;
    }

    public Double getCommitted() {
        return committed;
    }

    public Double getRemaining() {
        return remaining;
    }

    public Double getUtilizationPercent() {
        return utilizationPercent;
    }

    public Integer getWorkItemCount() {
        return workItemCount;
    }

    public List<AssigneeLoadResponse> getAssigneeLoads() {
        return assigneeLoads;
    }
}
