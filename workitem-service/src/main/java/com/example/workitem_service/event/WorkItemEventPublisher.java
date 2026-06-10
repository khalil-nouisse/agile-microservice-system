package com.example.workitem_service.event;

import com.example.workitem_service.model.WorkItem;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@Component
public class WorkItemEventPublisher {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public WorkItemEventPublisher(KafkaTemplate<String, String> kafkaTemplate, ObjectMapper objectMapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    public void publishTaskCreated(WorkItem item) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("taskId", item.getId());
        payload.put("title", item.getTitle());
        payload.put("projectId", item.getProjectId());
        payload.put("assigneeId", item.getAssigneeId());
        send("task.created", item.getId(), payload);
    }

    public void publishTaskAssigned(WorkItem item) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("taskId", item.getId());
        payload.put("title", item.getTitle());
        payload.put("assigneeId", item.getAssigneeId());
        payload.put("projectId", item.getProjectId());
        send("task.assigned", item.getId(), payload);
    }

    public void publishTaskStatusChanged(WorkItem item, String previousStatus) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("taskId", item.getId());
        payload.put("title", item.getTitle());
        payload.put("assigneeId", item.getAssigneeId());
        payload.put("previousStatus", previousStatus);
        payload.put("newStatus", item.getStatus());
        send("task.status-changed", item.getId(), payload);
    }

    private void send(String topic, UUID key, Map<String, Object> payload) {
        try {
            kafkaTemplate.send(topic, key.toString(), objectMapper.writeValueAsString(payload));
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Failed to serialize work item event for topic " + topic, ex);
        }
    }
}
