package com.example.sprint_service.dto;

import com.example.sprint_service.model.Sprint;
import com.example.sprint_service.model.SprintWorkItem;

import java.util.List;
import java.util.UUID;

public class SprintCapacityResponse {

    private UUID sprintId;
    private UUID projectId;
    private Double plannedCapacity;
    private Double totalEstimatedLoad;
    private Double remainingCapacity;
    private boolean overloaded;
    private int workItemCount;
    private List<AssigneeLoadResponse> assigneeLoads;

    public static SprintCapacityResponse from(
            Sprint sprint,
            List<SprintWorkItem> workItems,
            List<AssigneeLoadResponse> assigneeLoads
    ) {
        double totalEstimatedLoad = workItems.stream()
                .map(SprintWorkItem::getEstimation)
                .filter(value -> value != null)
                .mapToDouble(Double::doubleValue)
                .sum();

        SprintCapacityResponse response = new SprintCapacityResponse();
        response.sprintId = sprint.getId();
        response.projectId = sprint.getProjectId();
        response.plannedCapacity = sprint.getPlannedCapacity();
        response.totalEstimatedLoad = totalEstimatedLoad;
        response.remainingCapacity = sprint.getPlannedCapacity() - totalEstimatedLoad;
        response.overloaded = totalEstimatedLoad > sprint.getPlannedCapacity();
        response.workItemCount = workItems.size();
        response.assigneeLoads = assigneeLoads;
        return response;
    }

    public UUID getSprintId() {
        return sprintId;
    }

    public UUID getProjectId() {
        return projectId;
    }

    public Double getPlannedCapacity() {
        return plannedCapacity;
    }

    public Double getTotalEstimatedLoad() {
        return totalEstimatedLoad;
    }

    public Double getRemainingCapacity() {
        return remainingCapacity;
    }

    public boolean isOverloaded() {
        return overloaded;
    }

    public int getWorkItemCount() {
        return workItemCount;
    }

    public List<AssigneeLoadResponse> getAssigneeLoads() {
        return assigneeLoads;
    }
}
