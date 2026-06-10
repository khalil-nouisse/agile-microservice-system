package com.example.workitem_service.controller;

import com.example.workitem_service.dto.CreateWorkItemRequest;
import com.example.workitem_service.dto.UpdateWorkItemRequest;
import com.example.workitem_service.dto.UpdateWorkItemSprintRequest;
import com.example.workitem_service.dto.WorkItemResponse;
import com.example.workitem_service.service.WorkItemService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/workitems")
public class WorkItemController {

    private final WorkItemService workItemService;

    public WorkItemController(WorkItemService workItemService) {
        this.workItemService = workItemService;
    }

    @GetMapping("/project/{projectId}")
    public List<WorkItemResponse> getByProject(@PathVariable UUID projectId) {
        return workItemService.getWorkItemsByProject(projectId);
    }

    @GetMapping("/sprint/{sprintId}")
    public List<WorkItemResponse> getBySprint(@PathVariable UUID sprintId) {
        return workItemService.getWorkItemsBySprint(sprintId);
    }

    @GetMapping("/{workItemId}")
    public WorkItemResponse getWorkItem(@PathVariable UUID workItemId) {
        return workItemService.getWorkItem(workItemId);
    }

    @PostMapping
    public ResponseEntity<WorkItemResponse> createWorkItem(
            @RequestBody CreateWorkItemRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(workItemService.createWorkItem(request));
    }

    @PutMapping("/{workItemId}")
    public WorkItemResponse updateWorkItem(
            @PathVariable UUID workItemId,
            @RequestBody UpdateWorkItemRequest request
    ) {
        return workItemService.updateWorkItem(workItemId, request);
    }

    @PutMapping("/{workItemId}/sprint")
    public WorkItemResponse updateSprintAssignment(
            @PathVariable UUID workItemId,
            @RequestBody UpdateWorkItemSprintRequest request
    ) {
        return workItemService.updateSprintAssignment(workItemId, request.getSprintId());
    }

    @DeleteMapping("/{workItemId}")
    public ResponseEntity<Void> deleteWorkItem(@PathVariable UUID workItemId) {
        workItemService.deleteWorkItem(workItemId);
        return ResponseEntity.noContent().build();
    }
}
