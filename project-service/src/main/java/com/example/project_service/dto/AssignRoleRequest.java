package com.example.project_service.dto;

import com.example.project_service.model.ProjectRole;

public class AssignRoleRequest {

    private ProjectRole role;

    public ProjectRole getRole() {
        return role;
    }

    public void setRole(ProjectRole role) {
        this.role = role;
    }
}
