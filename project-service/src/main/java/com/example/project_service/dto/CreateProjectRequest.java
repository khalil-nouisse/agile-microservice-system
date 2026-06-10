package com.example.project_service.dto;

import com.example.project_service.model.AgileMethodology;

public class CreateProjectRequest {

    private String name;
    private String description;
    private AgileMethodology methodology;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public AgileMethodology getMethodology() {
        return methodology;
    }

    public void setMethodology(AgileMethodology methodology) {
        this.methodology = methodology;
    }
}
