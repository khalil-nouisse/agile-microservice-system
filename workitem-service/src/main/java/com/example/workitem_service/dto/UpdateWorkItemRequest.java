package com.example.workitem_service.dto;

import com.example.workitem_service.model.Priority;
import com.example.workitem_service.model.WorkItemStatus;
import lombok.Data;
import java.util.UUID;

@Data
public class UpdateWorkItemRequest {
    private String title;
    private String description;
    private Priority priority;
    private WorkItemStatus status;
    private Double estimation;
    private UUID assigneeId;
    private UUID sprintId;
}