package com.example.project_service.controller;

import com.example.project_service.dto.AssignRoleRequest;
import com.example.project_service.dto.CreateProjectRequest;
import com.example.project_service.dto.InviteMemberRequest;
import com.example.project_service.dto.ProjectMemberResponse;
import com.example.project_service.dto.ProjectResponse;
import com.example.project_service.dto.UpdateProjectRequest;
import com.example.project_service.exception.BadRequestException;
import com.example.project_service.service.ProjectService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/projects")
public class ProjectController {

    private final ProjectService projectService;

    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    @GetMapping
    public List<ProjectResponse> getAllProjects() {
        return projectService.getAllProjects();
    }

    @GetMapping("/{projectId}")
    public ProjectResponse getProject(@PathVariable UUID projectId) {
        return projectService.getProject(projectId);
    }

    @PostMapping
    public ResponseEntity<ProjectResponse> createProject(
            @RequestBody CreateProjectRequest request,
            @RequestHeader("X-User-Id") String userId
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(projectService.createProject(request, parseUserId(userId)));
    }

    @PutMapping("/{projectId}")
    public ProjectResponse updateProject(
            @PathVariable UUID projectId,
            @RequestBody UpdateProjectRequest request
    ) {
        return projectService.updateProject(projectId, request);
    }

    @DeleteMapping("/{projectId}")
    public ResponseEntity<Void> deleteProject(@PathVariable UUID projectId) {
        projectService.deleteProject(projectId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{projectId}/members")
    public List<ProjectMemberResponse> getProjectMembers(@PathVariable UUID projectId) {
        return projectService.getProjectMembers(projectId);
    }

    @PostMapping("/{projectId}/members/invitations")
    public ResponseEntity<ProjectMemberResponse> inviteMember(
            @PathVariable UUID projectId,
            @RequestBody InviteMemberRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(projectService.inviteMember(projectId, request));
    }

    @PutMapping("/{projectId}/members/{memberId}/role")
    public ProjectMemberResponse assignRole(
            @PathVariable UUID projectId,
            @PathVariable UUID memberId,
            @RequestBody AssignRoleRequest request
    ) {
        return projectService.assignRole(projectId, memberId, request);
    }

    private UUID parseUserId(String userId) {
        try {
            return UUID.fromString(userId);
        } catch (IllegalArgumentException ex) {
            throw new BadRequestException("X-User-Id must be a valid UUID");
        }
    }
}
