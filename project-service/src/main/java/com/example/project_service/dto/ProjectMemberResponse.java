package com.example.project_service.dto;

import com.example.project_service.model.MemberStatus;
import com.example.project_service.model.ProjectMember;
import com.example.project_service.model.ProjectRole;

import java.time.LocalDateTime;
import java.util.UUID;

public class ProjectMemberResponse {

    private UUID id;
    private UUID projectId;
    private String email;
    private UUID userId;
    private ProjectRole role;
    private MemberStatus status;
    private LocalDateTime invitedAt;

    public static ProjectMemberResponse from(ProjectMember member) {
        ProjectMemberResponse response = new ProjectMemberResponse();
        response.id = member.getId();
        response.projectId = member.getProject().getId();
        response.email = member.getEmail();
        response.userId = member.getUserId();
        response.role = member.getRole();
        response.status = member.getStatus();
        response.invitedAt = member.getInvitedAt();
        return response;
    }

    public UUID getId() {
        return id;
    }

    public UUID getProjectId() {
        return projectId;
    }

    public String getEmail() {
        return email;
    }

    public UUID getUserId() {
        return userId;
    }

    public ProjectRole getRole() {
        return role;
    }

    public MemberStatus getStatus() {
        return status;
    }

    public LocalDateTime getInvitedAt() {
        return invitedAt;
    }
}
