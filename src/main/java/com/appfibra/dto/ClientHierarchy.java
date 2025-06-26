package com.appfibra.dto;

import java.util.List;

public class ClientHierarchy {
    private String client;
    private List<Project> projects;

    public String getClient() {
        return client;
    }

    public void setClient(String client) {
        this.client = client;
    }

    public List<Project> getProjects() {
        return projects;
    }

    public void setProjects(List<Project> projects) {
        this.projects = projects;
    }
}
