package com.example.workitem_service.dto;

import java.util.UUID;

public class UpdateWorkItemSprintRequest {
    private UUID sprintId;

    public UUID getSprintId() {
        return sprintId;
    }

    public void setSprintId(UUID sprintId) {
        this.sprintId = sprintId;
    }
}
