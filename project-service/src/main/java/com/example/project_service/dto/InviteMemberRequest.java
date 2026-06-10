package com.example.project_service.dto;

import com.example.project_service.model.ProjectRole;

import java.util.UUID;

public class InviteMemberRequest {

    private String email;
    private UUID userId;
    private ProjectRole role;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public ProjectRole getRole() {
        return role;
    }

    public void setRole(ProjectRole role) {
        this.role = role;
    }
}
