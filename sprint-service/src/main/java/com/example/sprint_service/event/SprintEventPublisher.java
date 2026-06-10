package com.example.sprint_service.event;

import com.example.sprint_service.dto.SprintCapacityResponse;
import com.example.sprint_service.model.Sprint;
import com.example.sprint_service.model.SprintWorkItem;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Component
public class SprintEventPublisher {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public SprintEventPublisher(KafkaTemplate<String, String> kafkaTemplate, ObjectMapper objectMapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    public void publishSprintCreated(Sprint sprint) {
        publish("sprint.created", sprint.getId(), Map.of(
                "sprintId", sprint.getId(),
                "projectId", sprint.getProjectId(),
                "name", sprint.getName(),
                "plannedCapacity", sprint.getPlannedCapacity()
        ));
    }

    public void publishWorkItemAdded(Sprint sprint, SprintWorkItem workItem) {
        publish("sprint.work-item-added", sprint.getId(), Map.of(
                "sprintId", sprint.getId(),
                "projectId", sprint.getProjectId(),
                "workItemId", workItem.getWorkItemId(),
                "estimation", workItem.getEstimation() == null ? 0.0 : workItem.getEstimation()
        ));
    }

    public void publishCapacityOverloaded(Sprint sprint, SprintCapacityResponse capacity) {
        publish("capacity.overloaded", sprint.getId(), Map.of(
                "sprintId", sprint.getId(),
                "projectId", sprint.getProjectId(),
                "plannedCapacity", capacity.getPlannedCapacity(),
                "totalEstimatedLoad", capacity.getTotalEstimatedLoad(),
                "remainingCapacity", capacity.getRemainingCapacity()
        ));
    }

    private void publish(String eventType, UUID key, Map<String, Object> data) {
        Map<String, Object> event = Map.of(
                "eventType", eventType,
                "occurredAt", LocalDateTime.now().toString(),
                "data", data
        );

        try {
            kafkaTemplate.send(eventType, key.toString(), objectMapper.writeValueAsString(event));
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Could not convert event to JSON", e);
        }
    }
}
