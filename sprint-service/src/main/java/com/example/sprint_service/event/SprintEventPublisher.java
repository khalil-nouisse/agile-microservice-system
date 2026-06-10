package com.example.sprint_service.event;

import com.example.sprint_service.model.Sprint;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class SprintEventPublisher {
    private final KafkaTemplate<String, String> kafkaTemplate;

    public SprintEventPublisher(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publishSprintCreated(Sprint sprint) {
        kafkaTemplate.send("sprint.created", sprint.getId().toString(), sprint.getProjectId().toString());
    }

    public void publishSprintUpdated(Sprint sprint) {
        kafkaTemplate.send("sprint.updated", sprint.getId().toString(), sprint.getProjectId().toString());
    }
}
