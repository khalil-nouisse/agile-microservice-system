package com.example.notification_service.dto;

import com.example.notification_service.model.Notification;
import com.example.notification_service.model.NotificationType;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;
import java.util.UUID;

public record NotificationResponse(
        UUID id,
        UUID userId,
        String message,
        NotificationType type,
        @JsonProperty("isRead") boolean isRead,
        LocalDateTime createdAt,
        UUID referenceId,
        UUID projectId
) {
    public static NotificationResponse from(Notification n) {
        return new NotificationResponse(
                n.getId(),
                n.getUserId(),
                n.getMessage(),
                n.getType(),
                n.isRead(),
                n.getCreatedAt(),
                n.getReferenceId(),
                n.getProjectId()
        );
    }
}
