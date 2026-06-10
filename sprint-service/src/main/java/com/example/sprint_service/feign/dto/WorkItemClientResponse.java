package com.example.sprint_service.feign.dto;

import java.util.UUID;

public class WorkItemClientResponse {
    private UUID id;
    private String title;
    private String status;
    private Double estimation;
    private UUID projectId;
    private UUID assigneeId;
    private UUID sprintId;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Double getEstimation() {
        return estimation;
    }

    public void setEstimation(Double estimation) {
        this.estimation = estimation;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public UUID getProjectId() {
        return projectId;
    }

    public void setProjectId(UUID projectId) {
        this.projectId = projectId;
    }

    public UUID getAssigneeId() {
        return assigneeId;
    }

    public void setAssigneeId(UUID assigneeId) {
        this.assigneeId = assigneeId;
    }

    public UUID getSprintId() {
        return sprintId;
    }

    public void setSprintId(UUID sprintId) {
        this.sprintId = sprintId;
    }
}
