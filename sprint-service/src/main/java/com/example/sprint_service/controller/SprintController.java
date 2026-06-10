package com.example.sprint_service.controller;

import com.example.sprint_service.dto.AddWorkItemToSprintRequest;
import com.example.sprint_service.dto.CreateSprintRequest;
import com.example.sprint_service.dto.SprintCapacityResponse;
import com.example.sprint_service.dto.SprintResponse;
import com.example.sprint_service.dto.SprintWorkItemResponse;
import com.example.sprint_service.dto.UpdateSprintRequest;
import com.example.sprint_service.service.SprintService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/sprints")
public class SprintController {
    private final SprintService sprintService;

    public SprintController(SprintService sprintService) {
        this.sprintService = sprintService;
    }

    @PostMapping
    public ResponseEntity<SprintResponse> createSprint(@RequestBody CreateSprintRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(sprintService.createSprint(request));
    }

    @GetMapping("/project/{projectId}")
    public List<SprintResponse> getSprintsByProject(@PathVariable UUID projectId) {
        return sprintService.getSprintsByProject(projectId);
    }

    @GetMapping("/{sprintId}")
    public SprintResponse getSprint(@PathVariable UUID sprintId) {
        return sprintService.getSprint(sprintId);
    }

    @PutMapping("/{sprintId}")
    public SprintResponse updateSprint(@PathVariable UUID sprintId, @RequestBody UpdateSprintRequest request) {
        return sprintService.updateSprint(sprintId, request);
    }

    @DeleteMapping("/{sprintId}")
    public SprintResponse cancelSprint(@PathVariable UUID sprintId) {
        return sprintService.cancelSprint(sprintId);
    }

    @PostMapping("/{sprintId}/work-items")
    public ResponseEntity<SprintWorkItemResponse> addWorkItem(
            @PathVariable UUID sprintId,
            @RequestBody AddWorkItemToSprintRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(sprintService.addWorkItem(sprintId, request));
    }

    @GetMapping("/{sprintId}/work-items")
    public List<SprintWorkItemResponse> getWorkItems(@PathVariable UUID sprintId) {
        return sprintService.getSprintWorkItems(sprintId);
    }

    @DeleteMapping("/{sprintId}/work-items/{workItemId}")
    public ResponseEntity<Void> removeWorkItem(@PathVariable UUID sprintId, @PathVariable UUID workItemId) {
        sprintService.removeWorkItem(sprintId, workItemId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{sprintId}/capacity")
    public SprintCapacityResponse getCapacity(@PathVariable UUID sprintId) {
        return sprintService.getCapacity(sprintId);
    }
}
