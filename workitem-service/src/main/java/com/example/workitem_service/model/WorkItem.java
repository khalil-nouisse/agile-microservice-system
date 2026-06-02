package com.example.workitem_service.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "work_items")
@Data
public class WorkItem {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private WorkItemType type;

    @Enumerated(EnumType.STRING)
    private Priority priority = Priority.MEDIUM;

    @Enumerated(EnumType.STRING)
    private WorkItemStatus status = WorkItemStatus.TODO;

    private Double estimation;

    @Column(name = "project_id", nullable = false)
    private UUID projectId;

    @Column(name = "assignee_id")
    private UUID assigneeId;

    @Column(name = "sprint_id")
    private UUID sprintId;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
}