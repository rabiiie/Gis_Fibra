package com.appfibra.dto;

import java.util.List;

public class Project {
    private String project;
    private List<VillageLayer> villages;

    public String getProject() {
        return project;
    }

    public void setProject(String project) {
        this.project = project;
    }

    public List<VillageLayer> getVillages() {
        return villages;
    }

    public void setVillages(List<VillageLayer> villages) {
        this.villages = villages;
    }
}
// This class represents a project containing a list of villages and their associated layers.