package com.appfibra.Controller;

import com.appfibra.DAO.QgisProjectDAO;
import com.appfibra.model.QgisProject;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/qgis-projects")
public class QgisProjectController {
    private final QgisProjectDAO qgisProjectDAO;

    public QgisProjectController(QgisProjectDAO qgisProjectDAO) {
        this.qgisProjectDAO = qgisProjectDAO;
    }

    /**
     * Devuelve la lista de proyectos
     */
    @GetMapping
    public List<QgisProject> getAllProjects() {
        return qgisProjectDAO.findAll();
    }

    /**
     * Devuelve un GeoJSON con todas las capas de un proyecto.
     */
    @GetMapping("/geojson")
    public Map<String, Object> getProjectLayers(
            @RequestParam(required = false) String project) {
        return qgisProjectDAO.getProjectLayersGeoJSON(project);
    }

}
