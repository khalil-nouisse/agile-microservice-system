package com.example.workitem_service.service;

import com.example.workitem_service.dto.CreateWorkItemRequest;
import com.example.workitem_service.dto.UpdateWorkItemRequest;
import com.example.workitem_service.dto.WorkItemResponse;
import com.example.workitem_service.event.WorkItemEventPublisher;
import com.example.workitem_service.exception.BadRequestException;
import com.example.workitem_service.exception.ForbiddenException;
import com.example.workitem_service.exception.NotFoundException;
import com.example.workitem_service.feign.ProjectClient;
import com.example.workitem_service.model.WorkItem;
import com.example.workitem_service.repository.WorkItemRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.UUID;

@Service
public class WorkItemService {

    private final WorkItemRepository workItemRepository;
    private final WorkItemEventPublisher eventPublisher;
    private final ProjectClient projectClient;

    public WorkItemService(
            WorkItemRepository workItemRepository,
            WorkItemEventPublisher eventPublisher,
            ProjectClient projectClient
    ) {
        this.workItemRepository = workItemRepository;
        this.eventPublisher = eventPublisher;
        this.projectClient = projectClient;
    }

    @Transactional(readOnly = true)
    public List<WorkItemResponse> getWorkItemsByProject(UUID projectId) {
        return workItemRepository.findByProjectId(projectId)
                .stream()
                .map(WorkItemResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<WorkItemResponse> getWorkItemsBySprint(UUID sprintId) {
        return workItemRepository.findBySprintId(sprintId)
                .stream()
                .map(WorkItemResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public WorkItemResponse getWorkItem(UUID workItemId) {
        return WorkItemResponse.from(findWorkItem(workItemId));
    }

    @Transactional
    public WorkItemResponse createWorkItem(CreateWorkItemRequest request) {
        if (isBlank(request.getTitle())) {
            throw new BadRequestException("Title is required");
        }
        if (request.getType() == null) {
            throw new BadRequestException("Type is required");
        }
        if (request.getProjectId() == null) {
            throw new BadRequestException("Project ID is required");
        }

        // Verify project exists via Feign
        try {
            projectClient.getProject(request.getProjectId());
        } catch (Exception e) {
            throw new BadRequestException("Project not found");
        }

        WorkItem item = new WorkItem();
        item.setTitle(request.getTitle());
        item.setDescription(request.getDescription());
        item.setType(request.getType());
        item.setProjectId(request.getProjectId());
        item.setAssigneeId(request.getAssigneeId());
        item.setSprintId(request.getSprintId());
        item.setEstimation(request.getEstimation());

        if (request.getPriority() != null) {
            item.setPriority(request.getPriority());
        }

        WorkItem saved = workItemRepository.save(item);
        eventPublisher.publishTaskCreated(saved);

        if (saved.getAssigneeId() != null) {
            eventPublisher.publishTaskAssigned(saved);
        }

        return WorkItemResponse.from(saved);
    }

    @Transactional
    public WorkItemResponse updateWorkItem(UUID workItemId, UpdateWorkItemRequest request, UUID callerId) {
        WorkItem item = findWorkItem(workItemId);
        String previousStatus = item.getStatus().name();

        if (request.getAssigneeId() != null) {
            ProjectClient.ProjectSummary project = projectClient.getProject(item.getProjectId());
            if (!project.ownerId().equals(callerId)) {
                throw new ForbiddenException("Only the project owner can assign members to tasks");
            }
            item.setAssigneeId(request.getAssigneeId());
            eventPublisher.publishTaskAssigned(item);
        }
        if (!isBlank(request.getTitle())) {
            item.setTitle(request.getTitle());
        }
        if (request.getDescription() != null) {
            item.setDescription(request.getDescription());
        }
        if (request.getPriority() != null) {
            item.setPriority(request.getPriority());
        }
        if (request.getStatus() != null) {
            item.setStatus(request.getStatus());
            eventPublisher.publishTaskStatusChanged(item, previousStatus);
        }
        if (request.getEstimation() != null) {
            item.setEstimation(request.getEstimation());
        }
        if (request.getSprintId() != null) {
            item.setSprintId(request.getSprintId());
        }

        return WorkItemResponse.from(workItemRepository.save(item));
    }

    @Transactional
    public void deleteWorkItem(UUID workItemId) {
        WorkItem item = findWorkItem(workItemId);
        workItemRepository.delete(item);
    }

    private WorkItem findWorkItem(UUID workItemId) {
        return workItemRepository.findById(workItemId)
                .orElseThrow(() -> new NotFoundException("Work item not found"));
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}