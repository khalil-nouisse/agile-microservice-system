package com.example.sprint_service.dto;

import java.util.UUID;

public class AddWorkItemToSprintRequest {
    private UUID workItemId;

    public UUID getWorkItemId() {
        return workItemId;
    }

    public void setWorkItemId(UUID workItemId) {
        this.workItemId = workItemId;
    }
}
