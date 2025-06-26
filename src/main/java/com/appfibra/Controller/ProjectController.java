package com.appfibra.Controller;

import com.appfibra.service.ProjectService;
import com.appfibra.dto.VillageDTO;
import com.appfibra.service.CivilWorksService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/projects")
@CrossOrigin(origins = "*")
public class ProjectController {

    @Autowired
    private ProjectService projectService;

    /**
     * Devuelve TODOS los proyectos.
     * GET /api/projects
     */
    @GetMapping
    public List<Map<String, Object>> getAllProjects() {
        return projectService.getAllProjects();
    }

    /**
     * Devuelve los detalles de UN proyecto específico (opcional).
     * GET /api/projects/{projectId}
     */
    @GetMapping("/{projectId}")
    public Map<String, Object> getProjectById(@PathVariable int projectId) {
        return projectService.getProjectById(projectId);
    }

    /**
     * Devuelve la obra civil (Civil Works) de un proyecto específico.
     * GET /api/projects/{projectId}/civil-works
     */
    @GetMapping("/{projectId}/civil-works")
    public List<Map<String, Object>> getCivilWorksByProject(@PathVariable int projectId) {
        return projectService.getCivilWorksByProject(projectId);
    }
    
    @GetMapping("/{projectId}/villages")
    public List<VillageDTO> getVillages(@PathVariable Long projectId) {
        // Lógica para obtener villages desde la tabla 'civil_works'
        return projectService.getVillagesByProject(projectId);
    }
    
    @GetMapping("/{projectId}/pops")
    public List<Map<String, Object>> getPopsByProject(@PathVariable Long projectId) {
        return projectService.getPopsByProject(projectId);
    }


}
