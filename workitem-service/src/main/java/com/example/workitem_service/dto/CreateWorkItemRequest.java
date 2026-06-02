package com.example.workitem_service.dto;

import com.example.workitem_service.model.Priority;
import com.example.workitem_service.model.WorkItemType;
import lombok.Data;
import java.util.UUID;

@Data
public class CreateWorkItemRequest {
    private String title;
    private String description;
    private WorkItemType type;
    private Priority priority;
    private Double estimation;
    private UUID projectId;
    private UUID assigneeId;
    private UUID sprintId;
}