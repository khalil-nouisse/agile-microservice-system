package com.example.project_service.event;

import com.example.project_service.model.Project;
import com.example.project_service.model.ProjectMember;
import com.example.project_service.model.ProjectRole;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Component
public class ProjectEventPublisher {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public ProjectEventPublisher(KafkaTemplate<String, String> kafkaTemplate, ObjectMapper objectMapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    public void publishProjectCreated(Project project) {
        publish("project.created", project.getId(), Map.of(
                "projectId", project.getId(),
                "name", project.getName(),
                "ownerId", project.getOwnerId(),
                "methodology", project.getMethodology().name()
        ));
    }

    public void publishMemberInvited(ProjectMember member) {
        Map<String, Object> data = new java.util.HashMap<>();
        data.put("projectId", member.getProject().getId());
        data.put("memberId", member.getId());
        data.put("email", member.getEmail());
        data.put("role", member.getRole().name());
        // userId is null when invited by email only; consumer must handle that
        data.put("userId", member.getUserId() != null ? member.getUserId().toString() : null);
        publish("member.invited", member.getProject().getId(), data);
    }

    public void publishRoleAssigned(ProjectMember member, ProjectRole previousRole) {
        publish("role.assigned", member.getProject().getId(), Map.of(
                "projectId", member.getProject().getId(),
                "memberId", member.getId(),
                "email", member.getEmail(),
                "previousRole", previousRole.name(),
                "newRole", member.getRole().name()
        ));
    }

    private void publish(String eventType, UUID key, Map<String, Object> data) {
        Map<String, Object> event = Map.of(
                "eventType", eventType,
                "occurredAt", LocalDateTime.now().toString(),
                "data", data
        );

        try {
            // The topic name is the event name for now. This is easy to see
            // in Kafka while learning, and Notification Service can subscribe
            // to these topics later.
            kafkaTemplate.send(eventType, key.toString(), objectMapper.writeValueAsString(event));
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Could not convert event to JSON", e);
        }
    }
}
