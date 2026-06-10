package com.example.notification_service.kafka;

import com.example.notification_service.model.NotificationType;
import com.example.notification_service.service.NotificationService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class WorkItemEventConsumer {

    private final NotificationService notificationService;
    private final ObjectMapper objectMapper;

    // Payload: { "taskId": "...", "title": "...", "projectId": "...", "assigneeId": "..." }
    @KafkaListener(topics = "task.created", groupId = "notification-group")
    public void onTaskCreated(String message) {
        try {
            JsonNode root = objectMapper.readTree(message);
            UUID assigneeId = readUuid(root, "assigneeId");
            String title = root.get("title").asText();

            if (assigneeId != null) {
                notificationService.save(assigneeId,
                        "A new task '" + title + "' was created and assigned to you.",
                        NotificationType.TASK_CREATED);
            }
        } catch (Exception e) {
            log.error("Failed to process task.created: {}", e.getMessage());
        }
    }

    // Payload: { "taskId": "...", "assigneeId": "...", "projectId": "..." }
    @KafkaListener(topics = "task.assigned", groupId = "notification-group")
    public void onTaskAssigned(String message) {
        try {
            JsonNode root = objectMapper.readTree(message);
            UUID assigneeId = readUuid(root, "assigneeId");
            String title = root.path("title").asText(root.path("taskId").asText());
            if (assigneeId == null) {
                log.warn("Skipping task.assigned notification without assigneeId");
                return;
            }
            notificationService.save(assigneeId,
                    "You have been assigned to task: " + title + ".",
                    NotificationType.TASK_ASSIGNED);
        } catch (Exception e) {
            log.error("Failed to process task.assigned: {}", e.getMessage());
        }
    }

    // Payload: { "taskId": "...", "title": "...", "assigneeId": "...", "previousStatus": "...", "newStatus": "..." }
    @KafkaListener(topics = "task.status-changed", groupId = "notification-group")
    public void onTaskStatusChanged(String message) {
        try {
            JsonNode root = objectMapper.readTree(message);
            UUID assigneeId = readUuid(root, "assigneeId");
            String newStatus = root.get("newStatus").asText();
            String previousStatus = root.get("previousStatus").asText();
            String title = root.path("title").asText(root.path("taskId").asText());

            if (assigneeId != null) {
                notificationService.save(assigneeId,
                        "Task '" + title + "' status changed from " + previousStatus + " to " + newStatus + ".",
                        NotificationType.TASK_STATUS_CHANGED);
            }
        } catch (Exception e) {
            log.error("Failed to process task.status-changed: {}", e.getMessage());
        }
    }

    private UUID readUuid(JsonNode root, String fieldName) {
        JsonNode node = root.get(fieldName);
        if (node == null || node.isNull() || node.asText().isBlank()) {
            return null;
        }
        return UUID.fromString(node.asText());
    }
}
