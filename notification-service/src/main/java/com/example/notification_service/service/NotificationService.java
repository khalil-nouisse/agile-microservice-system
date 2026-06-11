package com.example.notification_service.service;

import com.example.notification_service.dto.NotificationResponse;
import com.example.notification_service.model.Notification;
import com.example.notification_service.model.NotificationType;
import com.example.notification_service.repository.NotificationRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final NotificationRepository repository;

    @Transactional
    public void save(UUID userId, String message, NotificationType type) {
        save(userId, message, type, null, null);
    }

    @Transactional
    public void save(UUID userId, String message, NotificationType type, UUID referenceId, UUID projectId) {
        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setMessage(message);
        notification.setType(type);
        notification.setReferenceId(referenceId);
        notification.setProjectId(projectId);
        repository.save(notification);
        log.info("Saved notification type={} userId={}", type, userId);
    }

    public List<NotificationResponse> getByUserId(UUID userId) {
        return repository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(NotificationResponse::from)
                .toList();
    }

    @Transactional
    public NotificationResponse markAsRead(UUID id) {
        Notification notification = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Notification not found: " + id));
        notification.setRead(true);
        return NotificationResponse.from(repository.save(notification));
    }
}
