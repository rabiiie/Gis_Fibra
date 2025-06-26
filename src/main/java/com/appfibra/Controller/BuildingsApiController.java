package com.appfibra.Controller;

import com.appfibra.service.BuildingsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/buildings")
public class BuildingsApiController {

    private final BuildingsService buildingsService;

    public BuildingsApiController(BuildingsService buildingsService) {
        this.buildingsService = buildingsService;
    }

    /**
     * GET /api/buildings/geojson
     * Devuelve GeoJSON de puntos de buildings según popId.
     */
    @GetMapping("/geojson")
    public ResponseEntity<?> getBuildingsGeoJSON(@RequestParam Integer projectId) {
        try {
            Map<String, Object> geojson = buildingsService.getBuildingsGeoJSON(projectId);
            return ResponseEntity.ok(geojson);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity
                .status(500)
                .body(Map.of("error", "Error al generar GeoJSON", "details", e.getMessage()));
        }
    }



}
