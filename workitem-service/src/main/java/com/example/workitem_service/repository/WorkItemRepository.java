package com.example.workitem_service.repository;

import com.example.workitem_service.model.WorkItem;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface WorkItemRepository extends JpaRepository<WorkItem, UUID> {
    List<WorkItem> findByProjectId(UUID projectId);
    List<WorkItem> findBySprintId(UUID sprintId);
    List<WorkItem> findByAssigneeId(UUID assigneeId);
}