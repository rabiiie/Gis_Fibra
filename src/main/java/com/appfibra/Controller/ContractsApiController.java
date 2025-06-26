package com.appfibra.Controller;

import com.appfibra.dto.DataTablesRequest;
import com.appfibra.service.ContractsService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/contracts")
public class ContractsApiController {

    private final ContractsService contractsService;

    public ContractsApiController(ContractsService contractsService) {
        this.contractsService = contractsService;
    }

    /**
     * POST /api/contracts/data
     * Recibe DataTablesRequest y devuelve JSON con draw, recordsTotal, recordsFiltered, data, etc.
     */
    @PostMapping("/data")
    public ResponseEntity<Map<String, Object>> getData(@RequestBody DataTablesRequest request) {
        try {
            Map<String, Object> result = contractsService.handleDataTablesRequest(request);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * POST /api/contracts/dashboard
     * Endpoint para obtener métricas del dashboard, el cual requiere:
     *  - 'field': el campo principal sobre el que se agrupará (ej: "anschlussstatus", "order_template", etc.)
     *  - 'view': el nombre de la vista (por ejemplo "vw_planner" o "planner") sobre la que se aplica.
     *  - 'filters': los filtros dinámicos (map de String a String).
     */
    @PostMapping("/dashboard")
    public ResponseEntity<Map<String, Object>> getDashboardMetrics(@RequestBody Map<String, Object> request) {
        try {
            String field = (String) request.get("field");
            String viewName = (String) request.get("view");
            if (viewName == null || viewName.isBlank()) {
                // Puedes definir el valor por defecto que prefieras:
                viewName = "vw_planner";
            }
            @SuppressWarnings("unchecked")
            Map<String, String> filters = (Map<String, String>) request.get("filters");
            // Ahora llamamos al Service indicando la vista, el campo y los filtros
            Map<String, Object> result = contractsService.getDashboardMetrics(viewName, field, filters);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * POST /api/contracts/aggregation
     * Endpoint para obtener agregación de datos.
     */
    @PostMapping("/aggregation")
    public ResponseEntity<?> getContractsAggregation(@RequestBody Map<String, Object> request) {
        try {
            String field1 = (String) request.get("field1");
            String field2 = (String) request.get("field2");
            @SuppressWarnings("unchecked")
            Map<String, String> filters = (Map<String, String>) request.get("filters");
            List<Map<String, Object>> aggregationData = contractsService.getContractsAggregation(field1, field2, filters);
            return ResponseEntity.ok(aggregationData);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * POST /api/contracts/multiview
     * Endpoint que permite obtener datos dinámicamente desde distintas vistas.
     */
    @PostMapping("/multiview")
    public ResponseEntity<?> handleMultiViewData(@RequestBody DataTablesRequest request) {
        try {
            Map<String, Object> result = contractsService.handleMultiViewDataTablesRequest(request);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error procesando la vista: " + e.getMessage()));
        }
    }
    
    /**
     * POST /api/contracts/extended
     * Endpoint para obtener datos extendidos desde la vista buildings_status_extended.
     */
    @PostMapping("/extended")
    public ResponseEntity<?> handleExtendedData(@RequestBody DataTablesRequest request) {
        try {
            Map<String, Object> result = contractsService.handleExtendedDataTablesRequest(request);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error procesando la vista: " + e.getMessage()));
        }
    }
    @PostMapping("/aggregation/has-summary")
    public ResponseEntity<?> getHasSummary(@RequestBody Map<String, Object> payload) {
        try {
            String groupField = (String) payload.getOrDefault("groupField", "access_location");
            Map<String, String> filters = (Map<String, String>) payload.getOrDefault("filters", Map.of());
            List<Map<String, Object>> result = contractsService.getHasAggregationByField(groupField, filters);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        }
    }


}
