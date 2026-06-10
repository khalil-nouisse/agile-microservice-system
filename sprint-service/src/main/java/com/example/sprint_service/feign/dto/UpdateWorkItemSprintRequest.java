package com.example.sprint_service.feign.dto;

import java.util.UUID;

public class UpdateWorkItemSprintRequest {
    private UUID sprintId;

    public UpdateWorkItemSprintRequest(UUID sprintId) {
        this.sprintId = sprintId;
    }

    public UUID getSprintId() {
        return sprintId;
    }
}
