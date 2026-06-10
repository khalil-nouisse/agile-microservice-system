package com.example.sprint_service.repository;

import com.example.sprint_service.model.SprintWorkItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SprintWorkItemRepository extends JpaRepository<SprintWorkItem, UUID> {

    List<SprintWorkItem> findBySprintId(UUID sprintId);

    Optional<SprintWorkItem> findBySprintIdAndWorkItemId(UUID sprintId, UUID workItemId);

    boolean existsBySprintIdAndWorkItemId(UUID sprintId, UUID workItemId);
}
