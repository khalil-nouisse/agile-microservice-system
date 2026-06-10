package com.example.sprint_service.service;

import com.example.sprint_service.dto.AddWorkItemToSprintRequest;
import com.example.sprint_service.dto.CreateSprintRequest;
import com.example.sprint_service.dto.SprintCapacityResponse;
import com.example.sprint_service.dto.SprintResponse;
import com.example.sprint_service.event.SprintEventPublisher;
import com.example.sprint_service.exception.BadRequestException;
import com.example.sprint_service.feign.ProjectClient;
import com.example.sprint_service.feign.WorkItemClient;
import com.example.sprint_service.feign.dto.WorkItemClientResponse;
import com.example.sprint_service.model.Sprint;
import com.example.sprint_service.model.SprintWorkItem;
import com.example.sprint_service.repository.SprintRepository;
import com.example.sprint_service.repository.SprintWorkItemRepository;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.lang.reflect.Proxy;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SprintServiceTest {

    @Test
    void createSprintValidatesProjectAndPublishesEvent() {
        UUID projectId = UUID.randomUUID();
        FakeSprintRepository sprintRepository = new FakeSprintRepository();
        FakeSprintWorkItemRepository workItemRepository = new FakeSprintWorkItemRepository();
        CapturingSprintEventPublisher eventPublisher = new CapturingSprintEventPublisher();
        SprintService sprintService = newService(
                sprintRepository,
                workItemRepository,
                projectId,
                null,
                eventPublisher
        );

        SprintResponse response = sprintService.createSprint(createRequest(projectId, 30.0));

        assertEquals("Sprint 1", response.getName());
        assertEquals(projectId, response.getProjectId());
        assertEquals(30.0, response.getCapacity().getRemainingCapacity());
        assertEquals(1, eventPublisher.sprintCreatedCount);
    }

    @Test
    void addWorkItemRejectsDuplicateInSprint() {
        UUID sprintId = UUID.randomUUID();
        UUID workItemId = UUID.randomUUID();
        FakeSprintRepository sprintRepository = new FakeSprintRepository();
        sprintRepository.sprint = sprint(sprintId, UUID.randomUUID(), 10.0);
        FakeSprintWorkItemRepository workItemRepository = new FakeSprintWorkItemRepository();
        workItemRepository.existingWorkItemId = workItemId;

        AddWorkItemToSprintRequest request = new AddWorkItemToSprintRequest();
        request.setWorkItemId(workItemId);

        assertThrows(BadRequestException.class, () -> newService(
                sprintRepository,
                workItemRepository,
                null,
                null,
                new CapturingSprintEventPublisher()
        ).addWorkItem(sprintId, request));
    }

    @Test
    void addWorkItemRejectsDifferentProject() {
        UUID sprintId = UUID.randomUUID();
        UUID sprintProjectId = UUID.randomUUID();
        UUID workItemId = UUID.randomUUID();
        FakeSprintRepository sprintRepository = new FakeSprintRepository();
        sprintRepository.sprint = sprint(sprintId, sprintProjectId, 10.0);
        FakeSprintWorkItemRepository workItemRepository = new FakeSprintWorkItemRepository();

        AddWorkItemToSprintRequest request = new AddWorkItemToSprintRequest();
        request.setWorkItemId(workItemId);

        assertThrows(BadRequestException.class, () -> newService(
                sprintRepository,
                workItemRepository,
                null,
                workItem(workItemId, UUID.randomUUID(), 5.0),
                new CapturingSprintEventPublisher()
        ).addWorkItem(sprintId, request));
        assertEquals(0, workItemRepository.savedItems.size());
    }

    @Test
    void calculateCapacityDetectsOverloadAndAssigneeLoad() {
        UUID sprintId = UUID.randomUUID();
        UUID projectId = UUID.randomUUID();
        UUID assigneeId = UUID.randomUUID();
        Sprint sprint = sprint(sprintId, projectId, 8.0);
        FakeSprintRepository sprintRepository = new FakeSprintRepository();
        sprintRepository.sprint = sprint;
        FakeSprintWorkItemRepository workItemRepository = new FakeSprintWorkItemRepository();
        workItemRepository.savedItems.add(sprintWorkItem(sprint, UUID.randomUUID(), "Task A", 5.0, assigneeId));
        workItemRepository.savedItems.add(sprintWorkItem(sprint, UUID.randomUUID(), "Task B", 6.0, assigneeId));

        SprintCapacityResponse response = newService(
                sprintRepository,
                workItemRepository,
                null,
                null,
                new CapturingSprintEventPublisher()
        ).calculateCapacity(sprintId);

        assertEquals(11.0, response.getTotalEstimatedLoad());
        assertEquals(-3.0, response.getRemainingCapacity());
        assertTrue(response.isOverloaded());
        assertEquals(1, response.getAssigneeLoads().size());
        assertEquals(11.0, response.getAssigneeLoads().get(0).getEstimatedLoad());
    }

    private SprintService newService(
            FakeSprintRepository sprintRepository,
            FakeSprintWorkItemRepository workItemRepository,
            UUID acceptedProjectId,
            WorkItemClientResponse workItem,
            CapturingSprintEventPublisher eventPublisher
    ) {
        ProjectClient projectClient = (ProjectClient) Proxy.newProxyInstance(
                ProjectClient.class.getClassLoader(),
                new Class<?>[]{ProjectClient.class},
                (proxy, method, args) -> {
                    if ("getProject".equals(method.getName()) && acceptedProjectId != null && acceptedProjectId.equals(args[0])) {
                        return new Object();
                    }
                    throw new RuntimeException("Project not found");
                }
        );

        WorkItemClient workItemClient = (WorkItemClient) Proxy.newProxyInstance(
                WorkItemClient.class.getClassLoader(),
                new Class<?>[]{WorkItemClient.class},
                (proxy, method, args) -> {
                    if ("getWorkItem".equals(method.getName()) && workItem != null && workItem.getId().equals(args[0])) {
                        return workItem;
                    }
                    throw new RuntimeException("Work item not found");
                }
        );

        return new SprintService(
                sprintRepository.proxy(),
                workItemRepository.proxy(),
                projectClient,
                workItemClient,
                eventPublisher
        );
    }

    private CreateSprintRequest createRequest(UUID projectId, double plannedCapacity) {
        CreateSprintRequest request = new CreateSprintRequest();
        request.setName("Sprint 1");
        request.setGoal("Deliver planning MVP");
        request.setProjectId(projectId);
        request.setStartDate(LocalDate.of(2026, 6, 10));
        request.setEndDate(LocalDate.of(2026, 6, 24));
        request.setPlannedCapacity(plannedCapacity);
        return request;
    }

    private Sprint sprint(UUID sprintId, UUID projectId, double plannedCapacity) {
        Sprint sprint = new Sprint();
        ReflectionTestUtils.setField(sprint, "id", sprintId);
        sprint.setName("Sprint 1");
        sprint.setProjectId(projectId);
        sprint.setStartDate(LocalDate.of(2026, 6, 10));
        sprint.setEndDate(LocalDate.of(2026, 6, 24));
        sprint.setPlannedCapacity(plannedCapacity);
        return sprint;
    }

    private WorkItemClientResponse workItem(UUID workItemId, UUID projectId, double estimation) {
        WorkItemClientResponse response = new WorkItemClientResponse();
        response.setId(workItemId);
        response.setProjectId(projectId);
        response.setTitle("Task A");
        response.setStatus("TODO");
        response.setEstimation(estimation);
        response.setAssigneeId(UUID.randomUUID());
        return response;
    }

    private SprintWorkItem sprintWorkItem(
            Sprint sprint,
            UUID workItemId,
            String title,
            double estimation,
            UUID assigneeId
    ) {
        SprintWorkItem item = new SprintWorkItem();
        ReflectionTestUtils.setField(item, "id", UUID.randomUUID());
        item.setSprint(sprint);
        item.setWorkItemId(workItemId);
        item.setTitle(title);
        item.setStatus("TODO");
        item.setEstimation(estimation);
        item.setAssigneeId(assigneeId);
        return item;
    }

    private static class FakeSprintRepository {
        private Sprint sprint;

        SprintRepository proxy() {
            return (SprintRepository) Proxy.newProxyInstance(
                    SprintRepository.class.getClassLoader(),
                    new Class<?>[]{SprintRepository.class},
                    (proxy, method, args) -> switch (method.getName()) {
                        case "save" -> {
                            Sprint value = (Sprint) args[0];
                            if (value.getId() == null) {
                                ReflectionTestUtils.setField(value, "id", UUID.randomUUID());
                            }
                            sprint = value;
                            yield value;
                        }
                        case "findById" -> Optional.ofNullable(sprint)
                                .filter(value -> value.getId().equals(args[0]));
                        case "findByProjectId" -> sprint != null && sprint.getProjectId().equals(args[0])
                                ? List.of(sprint)
                                : List.of();
                        default -> defaultReturn(method.getReturnType());
                    }
            );
        }
    }

    private static class FakeSprintWorkItemRepository {
        private UUID existingWorkItemId;
        private final List<SprintWorkItem> savedItems = new ArrayList<>();

        SprintWorkItemRepository proxy() {
            return (SprintWorkItemRepository) Proxy.newProxyInstance(
                    SprintWorkItemRepository.class.getClassLoader(),
                    new Class<?>[]{SprintWorkItemRepository.class},
                    (proxy, method, args) -> switch (method.getName()) {
                        case "save" -> {
                            SprintWorkItem value = (SprintWorkItem) args[0];
                            if (value.getId() == null) {
                                ReflectionTestUtils.setField(value, "id", UUID.randomUUID());
                            }
                            savedItems.add(value);
                            yield value;
                        }
                        case "findBySprintId" -> savedItems.stream()
                                .filter(item -> item.getSprint().getId().equals(args[0]))
                                .toList();
                        case "existsBySprintIdAndWorkItemId" -> existingWorkItemId != null && existingWorkItemId.equals(args[1]);
                        case "findBySprintIdAndWorkItemId" -> savedItems.stream()
                                .filter(item -> item.getSprint().getId().equals(args[0]) && item.getWorkItemId().equals(args[1]))
                                .findFirst();
                        case "delete" -> {
                            savedItems.remove(args[0]);
                            yield null;
                        }
                        default -> defaultReturn(method.getReturnType());
                    }
            );
        }
    }

    private static Object defaultReturn(Class<?> returnType) {
        if (returnType == boolean.class) {
            return false;
        }
        if (returnType == int.class || returnType == long.class || returnType == double.class) {
            return 0;
        }
        return null;
    }

    private static class CapturingSprintEventPublisher extends SprintEventPublisher {
        private int sprintCreatedCount;

        CapturingSprintEventPublisher() {
            super(null, null);
        }

        @Override
        public void publishSprintCreated(Sprint sprint) {
            sprintCreatedCount++;
        }

        @Override
        public void publishWorkItemAdded(Sprint sprint, SprintWorkItem workItem) {
        }

        @Override
        public void publishCapacityOverloaded(Sprint sprint, SprintCapacityResponse capacity) {
        }
    }
}
