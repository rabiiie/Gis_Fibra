package com.appfibra.Controller;

import com.appfibra.dto.DataTablesRequest;
import com.appfibra.service.CivilWorksService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/civil-works")
public class CivilWorksApiController {

    private final CivilWorksService civilWorksService;

    public CivilWorksApiController(CivilWorksService civilWorksService) {
        this.civilWorksService = civilWorksService;
    }

    /**
     * POST /api/civil-works/data
     * Para DataTables (paginación, buscadores, etc.).
     */
    @PostMapping("/data")
    public ResponseEntity<Map<String, Object>> getData(@RequestBody DataTablesRequest request) {
        try {
            String searchValue = "";
            if (request.getSearch() != null && request.getSearch().get("value") != null) {
                searchValue = request.getSearch().get("value").toString();
            }

            Map<String, Object> result = civilWorksService.handleDataTablesRequest(
                // fuente (“Engineering” o “Construction”), ajústalo según tu esquema
                "Engineering",
                request.getStart(),
                request.getLength(),
                searchValue,
                request.getColumns(),
                request.getDraw(),
                request.getClient() != null ? request.getClient() : "",
                request.getProject_id() != null ? request.getProject_id() : 0
            );

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * GET /api/civil-works/geojson
     * Devuelve un GeoJSON de toda la obra civil para un popId,
     * filtrable opcionalmente por type y spec.
     */
    @GetMapping("/geojson")
    public ResponseEntity<?> getCivilWorksGeoJSON(
            @RequestParam Integer projectId,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String spec
    ) {
        try {
            Map<String, Object> geojson = civilWorksService.getCivilWorksGeoJSON(projectId, type, spec);
            return ResponseEntity.ok(geojson);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Error al generar GeoJSON", "details", e.getMessage()));
        }
    }


    /**
     * GET /api/civil-works/villages?projectId=...
     * Para poblar el dropdown de villages (opcional).
     */
    @GetMapping("/villages")
    public ResponseEntity<?> getVillagesByProject(@RequestParam Long projectId) {
        try {
            List<Map<String, Object>> villages = civilWorksService.getVillagesByProject(projectId);
            return ResponseEntity.ok(villages);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Error al obtener villages", "details", e.getMessage()));
        }
    }

    /**
     * GET /api/civil-works/types?popId=...
     * Para poblar el dropdown de tipos (Drilling, Trenching, etc.).
     */
    @GetMapping("/types")
    public ResponseEntity<?> getTypesByPop(@RequestParam Integer popId) {
        try {
            List<String> types = civilWorksService.getTypesByPop(popId);
            return ResponseEntity.ok(types);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Error al obtener tipos", "details", e.getMessage()));
        }
    }

    /**
     * GET /api/civil-works/specs?popId=...
     * Para poblar el dropdown de specs (DAC, Directional Drilling, etc.).
     */
    @GetMapping("/specs")
    public ResponseEntity<?> getSpecsByPop(@RequestParam Integer popId) {
        try {
            List<String> specs = civilWorksService.getSpecsByPop(popId);
            return ResponseEntity.ok(specs);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Error al obtener specs", "details", e.getMessage()));
        }
    }

}
