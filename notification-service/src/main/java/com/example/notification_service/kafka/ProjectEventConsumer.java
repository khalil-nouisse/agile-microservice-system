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
public class ProjectEventConsumer {

    private final NotificationService notificationService;
    private final ObjectMapper objectMapper;

    // Payload: { "eventType": "project.created", "occurredAt": "...", "data": { "projectId", "name", "ownerId", "methodology" } }
    @KafkaListener(topics = "project.created", groupId = "notification-group")
    public void onProjectCreated(String message) {
        try {
            JsonNode data = objectMapper.readTree(message).get("data");
            UUID ownerId = UUID.fromString(data.get("ownerId").asText());
            String name = data.get("name").asText();
            notificationService.save(ownerId,
                    "Your project '" + name + "' has been created successfully.",
                    NotificationType.PROJECT_CREATED);
        } catch (Exception e) {
            log.error("Failed to process project.created: {}", e.getMessage());
        }
    }

    // Payload: { "eventType": "member.invited", "occurredAt": "...", "data": { "projectId", "memberId", "userId", "email", "role" } }
    @KafkaListener(topics = "member.invited", groupId = "notification-group")
    public void onMemberInvited(String message) {
        try {
            JsonNode data = objectMapper.readTree(message).get("data");
            JsonNode userIdNode = data.get("userId");
            if (userIdNode == null || userIdNode.isNull()) {
                log.warn("member.invited event has no userId — cannot route notification");
                return;
            }
            UUID userId    = UUID.fromString(userIdNode.asText());
            UUID memberId  = UUID.fromString(data.get("memberId").asText());
            UUID projectId = UUID.fromString(data.get("projectId").asText());
            String role    = data.get("role").asText();
            notificationService.save(userId,
                    "You have been invited to a project with role: " + role + ".",
                    NotificationType.MEMBER_INVITED,
                    memberId,
                    projectId);
        } catch (Exception e) {
            log.error("Failed to process member.invited: {}", e.getMessage());
        }
    }

    // Payload: { "eventType": "role.assigned", "occurredAt": "...", "data": { "projectId", "memberId", "email", "previousRole", "newRole" } }
    @KafkaListener(topics = "role.assigned", groupId = "notification-group")
    public void onRoleAssigned(String message) {
        try {
            JsonNode data = objectMapper.readTree(message).get("data");
            UUID memberId = UUID.fromString(data.get("memberId").asText());
            String newRole = data.get("newRole").asText();
            notificationService.save(memberId,
                    "Your project role has been updated to: " + newRole + ".",
                    NotificationType.ROLE_ASSIGNED);
        } catch (Exception e) {
            log.error("Failed to process role.assigned: {}", e.getMessage());
        }
    }
}
