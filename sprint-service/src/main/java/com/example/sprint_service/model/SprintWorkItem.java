package com.example.sprint_service.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "sprint_work_items",
        uniqueConstraints = @UniqueConstraint(columnNames = {"sprint_id", "work_item_id"})
)
public class SprintWorkItem {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "sprint_id", nullable = false)
    private UUID sprintId;

    @Column(name = "work_item_id", nullable = false)
    private UUID workItemId;

    @Column(nullable = false)
    private String title;

    private String status;

    @Column(nullable = false)
    private Double estimation;

    private UUID assigneeId;

    @Column(nullable = false, updatable = false)
    private LocalDateTime addedAt;

    @PrePersist
    void prePersist() {
        addedAt = LocalDateTime.now();
    }

    public UUID getId() {
        return id;
    }

    public UUID getSprintId() {
        return sprintId;
    }

    public void setSprintId(UUID sprintId) {
        this.sprintId = sprintId;
    }

    public UUID getWorkItemId() {
        return workItemId;
    }

    public void setWorkItemId(UUID workItemId) {
        this.workItemId = workItemId;
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

    public UUID getAssigneeId() {
        return assigneeId;
    }

    public void setAssigneeId(UUID assigneeId) {
        this.assigneeId = assigneeId;
    }

    public LocalDateTime getAddedAt() {
        return addedAt;
    }
}
