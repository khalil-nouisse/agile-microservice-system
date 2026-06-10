package com.example.sprint_service.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;
import java.util.UUID;

public class CreateSprintRequest {
    private UUID projectId;
    private String name;
    private String goal;
    private LocalDate startDate;
    private LocalDate endDate;
    private Double capacity;

    public UUID getProjectId() {
        return projectId;
    }

    public void setProjectId(UUID projectId) {
        this.projectId = projectId;
    }

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

    public Double getCapacity() {
        return capacity;
    }

    @JsonProperty("plannedCapacity")
    public Double getPlannedCapacity() {
        return capacity;
    }

    @JsonAlias("plannedCapacity")
    public void setCapacity(Double capacity) {
        this.capacity = capacity;
    }

    public void setPlannedCapacity(Double plannedCapacity) {
        this.capacity = plannedCapacity;
    }
}
