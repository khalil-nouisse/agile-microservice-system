package com.example.sprint_service.dto;

import java.time.LocalDate;
import java.util.UUID;

public class CreateSprintRequest {

    private String name;
    private String goal;
    private UUID projectId;
    private LocalDate startDate;
    private LocalDate endDate;
    private Double plannedCapacity;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getGoal() {
        return goal;
    }

    public void setGoal(String goal) {
        this.goal = goal;
    }

    public UUID getProjectId() {
        return projectId;
    }

    public void setProjectId(UUID projectId) {
        this.projectId = projectId;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public Double getPlannedCapacity() {
        return plannedCapacity;
    }

    public void setPlannedCapacity(Double plannedCapacity) {
        this.plannedCapacity = plannedCapacity;
    }
}
