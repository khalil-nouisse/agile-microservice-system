package com.example.sprint_service.service;

import com.example.sprint_service.dto.AddWorkItemToSprintRequest;
import com.example.sprint_service.dto.AssigneeLoadResponse;
import com.example.sprint_service.dto.CreateSprintRequest;
import com.example.sprint_service.dto.SprintCapacityResponse;
import com.example.sprint_service.dto.SprintResponse;
import com.example.sprint_service.dto.SprintWorkItemResponse;
import com.example.sprint_service.dto.UpdateSprintRequest;
import com.example.sprint_service.event.SprintEventPublisher;
import com.example.sprint_service.exception.BadRequestException;
import com.example.sprint_service.exception.NotFoundException;
import com.example.sprint_service.feign.ProjectClient;
import com.example.sprint_service.feign.WorkItemClient;
import com.example.sprint_service.feign.dto.UpdateWorkItemSprintRequest;
import com.example.sprint_service.feign.dto.WorkItemClientResponse;
import com.example.sprint_service.model.Sprint;
import com.example.sprint_service.model.SprintStatus;
import com.example.sprint_service.model.SprintWorkItem;
import com.example.sprint_service.repository.SprintRepository;
import com.example.sprint_service.repository.SprintWorkItemRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class SprintService {
    private final SprintRepository sprintRepository;
    private final SprintWorkItemRepository sprintWorkItemRepository;
    private final ProjectClient projectClient;
    private final WorkItemClient workItemClient;
    private final SprintEventPublisher eventPublisher;

    public SprintService(
            SprintRepository sprintRepository,
            SprintWorkItemRepository sprintWorkItemRepository,
            ProjectClient projectClient,
            WorkItemClient workItemClient,
            SprintEventPublisher eventPublisher
    ) {
        this.sprintRepository = sprintRepository;
        this.sprintWorkItemRepository = sprintWorkItemRepository;
        this.projectClient = projectClient;
        this.workItemClient = workItemClient;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public SprintResponse createSprint(CreateSprintRequest request) {
        validateCreateRequest(request);
        verifyProjectExists(request.getProjectId());

        Sprint sprint = new Sprint();
        sprint.setProjectId(request.getProjectId());
        sprint.setName(request.getName().trim());
        sprint.setGoal(request.getGoal());
        sprint.setStartDate(request.getStartDate());
        sprint.setEndDate(request.getEndDate());
        sprint.setCapacity(request.getCapacity());

        Sprint saved = sprintRepository.save(sprint);
        eventPublisher.publishSprintCreated(saved);
        return SprintResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public List<SprintResponse> getSprintsByProject(UUID projectId) {
        return sprintRepository.findByProjectIdOrderByStartDateDesc(projectId)
                .stream()
                .map(SprintResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public SprintResponse getSprint(UUID sprintId) {
        return SprintResponse.from(findSprint(sprintId));
    }

    @Transactional
    public SprintResponse updateSprint(UUID sprintId, UpdateSprintRequest request) {
        Sprint sprint = findSprint(sprintId);

        if (!isBlank(request.getName())) {
            sprint.setName(request.getName().trim());
        }
        if (request.getGoal() != null) {
            sprint.setGoal(request.getGoal());
        }
        if (request.getStartDate() != null) {
            sprint.setStartDate(request.getStartDate());
        }
        if (request.getEndDate() != null) {
            sprint.setEndDate(request.getEndDate());
        }
        if (request.getCapacity() != null) {
            if (request.getCapacity() <= 0) {
                throw new BadRequestException("Capacity must be greater than zero");
            }
            sprint.setCapacity(request.getCapacity());
        }
        if (request.getStatus() != null) {
            sprint.setStatus(request.getStatus());
        }
        validateDateRange(sprint);

        Sprint saved = sprintRepository.save(sprint);
        eventPublisher.publishSprintUpdated(saved);
        return SprintResponse.from(saved);
    }

    @Transactional
    public SprintResponse cancelSprint(UUID sprintId) {
        Sprint sprint = findSprint(sprintId);
        sprint.setStatus(SprintStatus.CANCELLED);
        Sprint saved = sprintRepository.save(sprint);
        eventPublisher.publishSprintUpdated(saved);
        return SprintResponse.from(saved);
    }

    @Transactional
    public SprintWorkItemResponse addWorkItem(UUID sprintId, AddWorkItemToSprintRequest request) {
        if (request.getWorkItemId() == null) {
            throw new BadRequestException("Work item ID is required");
        }

        Sprint sprint = findSprint(sprintId);
        WorkItemClientResponse workItem = getWorkItemOrThrow(request.getWorkItemId());

        if (!sprint.getProjectId().equals(workItem.getProjectId())) {
            throw new BadRequestException("Work item must belong to the same project as the sprint");
        }
        if (sprintWorkItemRepository.existsBySprintIdAndWorkItemId(sprintId, request.getWorkItemId())) {
            throw new BadRequestException("Work item is already in this sprint");
        }

        SprintWorkItem sprintWorkItem = new SprintWorkItem();
        sprintWorkItem.setSprintId(sprintId);
        sprintWorkItem.setWorkItemId(workItem.getId());
        sprintWorkItem.setTitle(workItem.getTitle());
        sprintWorkItem.setStatus(workItem.getStatus());
        sprintWorkItem.setAssigneeId(workItem.getAssigneeId());
        sprintWorkItem.setEstimation(workItem.getEstimation() == null ? 0 : workItem.getEstimation());

        SprintWorkItem saved = sprintWorkItemRepository.save(sprintWorkItem);
        workItemClient.updateWorkItem(workItem.getId(), new UpdateWorkItemSprintRequest(sprintId));
        return SprintWorkItemResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public List<SprintWorkItemResponse> getSprintWorkItems(UUID sprintId) {
        findSprint(sprintId);
        return sprintWorkItemRepository.findBySprintId(sprintId)
                .stream()
                .map(SprintWorkItemResponse::from)
                .toList();
    }

    @Transactional
    public void removeWorkItem(UUID sprintId, UUID workItemId) {
        findSprint(sprintId);
        SprintWorkItem item = sprintWorkItemRepository.findBySprintIdAndWorkItemId(sprintId, workItemId)
                .orElseThrow(() -> new NotFoundException("Work item is not in this sprint"));
        sprintWorkItemRepository.delete(item);
        workItemClient.updateWorkItem(workItemId, new UpdateWorkItemSprintRequest(null));
    }

    @Transactional(readOnly = true)
    public SprintCapacityResponse getCapacity(UUID sprintId) {
        Sprint sprint = findSprint(sprintId);
        List<SprintWorkItem> items = sprintWorkItemRepository.findBySprintId(sprintId);
        double committed = items.stream()
                .mapToDouble(item -> item.getEstimation() == null ? 0 : item.getEstimation())
                .sum();

        Map<UUID, List<SprintWorkItem>> byAssignee = items.stream()
                .filter(item -> item.getAssigneeId() != null)
                .collect(Collectors.groupingBy(SprintWorkItem::getAssigneeId));

        List<AssigneeLoadResponse> assigneeLoads = byAssignee.entrySet()
                .stream()
                .map(entry -> new AssigneeLoadResponse(
                        entry.getKey(),
                        entry.getValue().stream()
                                .mapToDouble(item -> item.getEstimation() == null ? 0 : item.getEstimation())
                                .sum(),
                        entry.getValue().size()
                ))
                .toList();

        return new SprintCapacityResponse(
                sprint.getId(),
                sprint.getCapacity(),
                committed,
                items.size(),
                assigneeLoads
        );
    }

    private void validateCreateRequest(CreateSprintRequest request) {
        if (request.getProjectId() == null) {
            throw new BadRequestException("Project ID is required");
        }
        if (isBlank(request.getName())) {
            throw new BadRequestException("Name is required");
        }
        if (request.getStartDate() == null) {
            throw new BadRequestException("Start date is required");
        }
        if (request.getEndDate() == null) {
            throw new BadRequestException("End date is required");
        }
        if (request.getCapacity() == null || request.getCapacity() <= 0) {
            throw new BadRequestException("Capacity must be greater than zero");
        }
        if (request.getEndDate().isBefore(request.getStartDate())) {
            throw new BadRequestException("End date must be on or after start date");
        }
    }

    private void validateDateRange(Sprint sprint) {
        if (sprint.getEndDate().isBefore(sprint.getStartDate())) {
            throw new BadRequestException("End date must be on or after start date");
        }
    }

    private void verifyProjectExists(UUID projectId) {
        try {
            projectClient.getProject(projectId);
        } catch (Exception ex) {
            throw new BadRequestException("Project not found");
        }
    }

    private WorkItemClientResponse getWorkItemOrThrow(UUID workItemId) {
        try {
            return workItemClient.getWorkItem(workItemId);
        } catch (Exception ex) {
            throw new BadRequestException("Work item not found");
        }
    }

    private Sprint findSprint(UUID sprintId) {
        return sprintRepository.findById(sprintId)
                .orElseThrow(() -> new NotFoundException("Sprint not found"));
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
