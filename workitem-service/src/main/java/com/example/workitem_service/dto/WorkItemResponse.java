package com.example.workitem_service.dto;

import com.example.workitem_service.model.WorkItem;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class WorkItemResponse {
    private UUID id;
    private String title;
    private String description;
    private String type;
    private String priority;
    private String status;
    private Double estimation;
    private UUID projectId;
    private UUID assigneeId;
    private UUID sprintId;
    private LocalDateTime createdAt;

    public static WorkItemResponse from(WorkItem item) {
        WorkItemResponse response = new WorkItemResponse();
        response.setId(item.getId());
        response.setTitle(item.getTitle());
        response.setDescription(item.getDescription());
        response.setType(item.getType().name());
        response.setPriority(item.getPriority().name());
        response.setStatus(item.getStatus().name());
        response.setEstimation(item.getEstimation());
        response.setProjectId(item.getProjectId());
        response.setAssigneeId(item.getAssigneeId());
        response.setSprintId(item.getSprintId());
        response.setCreatedAt(item.getCreatedAt());
        return response;
    }
}