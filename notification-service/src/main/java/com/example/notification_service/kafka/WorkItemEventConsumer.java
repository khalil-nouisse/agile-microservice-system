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

    // Payload: { "taskId": "...", "title": "...", "projectId": "..." }
    @KafkaListener(topics = "task.created", groupId = "notification-group")
    public void onTaskCreated(String message) {
        try {
            JsonNode root = objectMapper.readTree(message);
            UUID projectId = UUID.fromString(root.get("projectId").asText());
            String title = root.get("title").asText();
            notificationService.save(projectId,
                    "New task '" + title + "' was created in your project.",
                    NotificationType.TASK_CREATED);
        } catch (Exception e) {
            log.error("Failed to process task.created: {}", e.getMessage());
        }
    }

    // Payload: { "taskId": "...", "title": "...", "assigneeId": "...", "projectId": "..." }
    @KafkaListener(topics = "task.assigned", groupId = "notification-group")
    public void onTaskAssigned(String message) {
        try {
            JsonNode root = objectMapper.readTree(message);
            UUID assigneeId = UUID.fromString(root.get("assigneeId").asText());
            String title = root.get("title").asText();
            notificationService.save(assigneeId,
                    "You have been assigned to task: '" + title + "'.",
                    NotificationType.TASK_ASSIGNED);
        } catch (Exception e) {
            log.error("Failed to process task.assigned: {}", e.getMessage());
        }
    }

    // Payload: { "taskId": "...", "previousStatus": "...", "newStatus": "..." }
    @KafkaListener(topics = "task.status-changed", groupId = "notification-group")
    public void onTaskStatusChanged(String message) {
        try {
            JsonNode root = objectMapper.readTree(message);
            UUID taskId = UUID.fromString(root.get("taskId").asText());
            String newStatus = root.get("newStatus").asText();
            String previousStatus = root.get("previousStatus").asText();
            notificationService.save(taskId,
                    "Task status changed from " + previousStatus + " to " + newStatus + ".",
                    NotificationType.TASK_STATUS_CHANGED);
        } catch (Exception e) {
            log.error("Failed to process task.status-changed: {}", e.getMessage());
        }
    }
}
