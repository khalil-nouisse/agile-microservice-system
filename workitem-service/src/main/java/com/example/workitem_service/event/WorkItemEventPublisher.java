package com.example.workitem_service.event;

import com.example.workitem_service.model.WorkItem;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class WorkItemEventPublisher {

    private final KafkaTemplate<String, String> kafkaTemplate;

    public WorkItemEventPublisher(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publishTaskCreated(WorkItem item) {
        kafkaTemplate.send("task.created", item.getId().toString(),
                String.format("{\"taskId\":\"%s\",\"title\":\"%s\",\"projectId\":\"%s\"}",
                        item.getId(), item.getTitle(), item.getProjectId()));
    }

    public void publishTaskAssigned(WorkItem item) {
        kafkaTemplate.send("task.assigned", item.getId().toString(),
                String.format("{\"taskId\":\"%s\",\"assigneeId\":\"%s\",\"projectId\":\"%s\"}",
                        item.getId(), item.getAssigneeId(), item.getProjectId()));
    }

    public void publishTaskStatusChanged(WorkItem item, String previousStatus) {
        kafkaTemplate.send("task.status-changed", item.getId().toString(),
                String.format("{\"taskId\":\"%s\",\"previousStatus\":\"%s\",\"newStatus\":\"%s\"}",
                        item.getId(), previousStatus, item.getStatus()));
    }
}