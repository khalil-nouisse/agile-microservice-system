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
import com.example.sprint_service.feign.dto.WorkItemClientResponse;
import com.example.sprint_service.model.Sprint;
import com.example.sprint_service.model.SprintStatus;
import com.example.sprint_service.model.SprintWorkItem;
import com.example.sprint_service.repository.SprintRepository;
import com.example.sprint_service.repository.SprintWorkItemRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class SprintService {

    private final SprintRepository sprintRepository;
    private final SprintWorkItemRepository workItemRepository;
    private final ProjectClient projectClient;
    private final WorkItemClient workItemClient;
    private final SprintEventPublisher eventPublisher;

    public SprintService(
            SprintRepository sprintRepository,
            SprintWorkItemRepository workItemRepository,
            ProjectClient projectClient,
            WorkItemClient workItemClient,
            SprintEventPublisher eventPublisher
    ) {
        this.sprintRepository = sprintRepository;
        this.workItemRepository = workItemRepository;
        this.projectClient = projectClient;
        this.workItemClient = workItemClient;
        this.eventPublisher = eventPublisher;
    }

    @Transactional(readOnly = true)
    public List<SprintResponse> getSprintsByProject(UUID projectId) {
        return sprintRepository.findByProjectId(projectId)
                .stream()
                .map(this::buildSprintResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public SprintResponse getSprint(UUID sprintId) {
        return buildSprintResponse(findSprint(sprintId));
    }

    @Transactional
    public SprintResponse createSprint(CreateSprintRequest request) {
        validateCreateRequest(request);
        verifyProjectExists(request.getProjectId());

        Sprint sprint = new Sprint();
        sprint.setName(request.getName().trim());
        sprint.setGoal(request.getGoal());
        sprint.setProjectId(request.getProjectId());
        sprint.setStartDate(request.getStartDate());
        sprint.setEndDate(request.getEndDate());
        sprint.setPlannedCapacity(request.getPlannedCapacity());

        Sprint savedSprint = sprintRepository.save(sprint);
        eventPublisher.publishSprintCreated(savedSprint);

        return buildSprintResponse(savedSprint);
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
        if (request.getPlannedCapacity() != null) {
            validateCapacity(request.getPlannedCapacity());
            sprint.setPlannedCapacity(request.getPlannedCapacity());
        }
        if (request.getStatus() != null) {
            sprint.setStatus(request.getStatus());
        }

        validateDateRange(sprint.getStartDate(), sprint.getEndDate());

        Sprint savedSprint = sprintRepository.save(sprint);
        SprintCapacityResponse capacity = calculateCapacity(savedSprint.getId());
        if (capacity.isOverloaded() && savedSprint.getStatus() != SprintStatus.CANCELLED) {
            eventPublisher.publishCapacityOverloaded(savedSprint, capacity);
        }

        return buildSprintResponse(savedSprint);
    }

    @Transactional
    public void cancelSprint(UUID sprintId) {
        Sprint sprint = findSprint(sprintId);
        sprint.setStatus(SprintStatus.CANCELLED);
        sprintRepository.save(sprint);
    }

    @Transactional
    public SprintWorkItemResponse addWorkItem(UUID sprintId, AddWorkItemToSprintRequest request) {
        if (request.getWorkItemId() == null) {
            throw new BadRequestException("Work item ID is required");
        }

        Sprint sprint = findSprint(sprintId);
        if (sprint.getStatus() == SprintStatus.COMPLETED || sprint.getStatus() == SprintStatus.CANCELLED) {
            throw new BadRequestException("Cannot change backlog for a closed sprint");
        }
        if (workItemRepository.existsBySprintIdAndWorkItemId(sprintId, request.getWorkItemId())) {
            throw new BadRequestException("Work item already exists in this sprint");
        }

        WorkItemClientResponse workItem = getWorkItemOrThrow(request.getWorkItemId());
        if (!sprint.getProjectId().equals(workItem.getProjectId())) {
            throw new BadRequestException("Work item belongs to a different project");
        }

        SprintWorkItem sprintWorkItem = new SprintWorkItem();
        sprintWorkItem.setSprint(sprint);
        sprintWorkItem.setWorkItemId(workItem.getId());
        sprintWorkItem.setTitle(workItem.getTitle());
        sprintWorkItem.setStatus(workItem.getStatus());
        sprintWorkItem.setEstimation(workItem.getEstimation());
        sprintWorkItem.setAssigneeId(workItem.getAssigneeId());

        SprintWorkItem savedWorkItem = workItemRepository.save(sprintWorkItem);
        eventPublisher.publishWorkItemAdded(sprint, savedWorkItem);

        SprintCapacityResponse capacity = calculateCapacity(sprintId);
        if (capacity.isOverloaded()) {
            eventPublisher.publishCapacityOverloaded(sprint, capacity);
        }

        return SprintWorkItemResponse.from(savedWorkItem);
    }

    @Transactional
    public void removeWorkItem(UUID sprintId, UUID workItemId) {
        findSprint(sprintId);
        SprintWorkItem sprintWorkItem = workItemRepository.findBySprintIdAndWorkItemId(sprintId, workItemId)
                .orElseThrow(() -> new NotFoundException("Work item is not in this sprint"));
        workItemRepository.delete(sprintWorkItem);
    }

    @Transactional(readOnly = true)
    public List<SprintWorkItemResponse> getSprintWorkItems(UUID sprintId) {
        findSprint(sprintId);
        return workItemRepository.findBySprintId(sprintId)
                .stream()
                .map(SprintWorkItemResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public SprintCapacityResponse calculateCapacity(UUID sprintId) {
        Sprint sprint = findSprint(sprintId);
        List<SprintWorkItem> workItems = workItemRepository.findBySprintId(sprintId);
        return SprintCapacityResponse.from(sprint, workItems, buildAssigneeLoads(workItems));
    }

    private SprintResponse buildSprintResponse(Sprint sprint) {
        List<SprintWorkItem> workItems = workItemRepository.findBySprintId(sprint.getId());
        SprintCapacityResponse capacity = SprintCapacityResponse.from(sprint, workItems, buildAssigneeLoads(workItems));
        List<SprintWorkItemResponse> workItemResponses = workItems.stream()
                .map(SprintWorkItemResponse::from)
                .toList();

        return SprintResponse.from(sprint, capacity, workItemResponses);
    }

    private List<AssigneeLoadResponse> buildAssigneeLoads(List<SprintWorkItem> workItems) {
        Map<UUID, List<SprintWorkItem>> byAssignee = workItems.stream()
                .filter(item -> item.getAssigneeId() != null)
                .collect(Collectors.groupingBy(SprintWorkItem::getAssigneeId));

        return byAssignee.entrySet()
                .stream()
                .map(entry -> new AssigneeLoadResponse(
                        entry.getKey(),
                        entry.getValue().stream()
                                .map(SprintWorkItem::getEstimation)
                                .filter(value -> value != null)
                                .mapToDouble(Double::doubleValue)
                                .sum(),
                        entry.getValue().size()
                ))
                .toList();
    }

    private Sprint findSprint(UUID sprintId) {
        return sprintRepository.findById(sprintId)
                .orElseThrow(() -> new NotFoundException("Sprint not found"));
    }

    private void validateCreateRequest(CreateSprintRequest request) {
        if (isBlank(request.getName())) {
            throw new BadRequestException("Sprint name is required");
        }
        if (request.getProjectId() == null) {
            throw new BadRequestException("Project ID is required");
        }
        if (request.getStartDate() == null) {
            throw new BadRequestException("Start date is required");
        }
        if (request.getEndDate() == null) {
            throw new BadRequestException("End date is required");
        }
        validateDateRange(request.getStartDate(), request.getEndDate());
        validateCapacity(request.getPlannedCapacity());
    }

    private void validateDateRange(LocalDate startDate, LocalDate endDate) {
        if (endDate.isBefore(startDate)) {
            throw new BadRequestException("End date must be after or equal to start date");
        }
    }

    private void validateCapacity(Double plannedCapacity) {
        if (plannedCapacity == null) {
            throw new BadRequestException("Planned capacity is required");
        }
        if (plannedCapacity < 0) {
            throw new BadRequestException("Planned capacity cannot be negative");
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

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
