package com.appfibra.Controller;

import com.appfibra.dto.DataTablesRequest;
import com.appfibra.service.ContractsService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/contracts")
public class ContractsController {

    private final ContractsService contractsService;

    public ContractsController(ContractsService contractsService) {
        this.contractsService = contractsService;
    }

    /**
     * GET /contracts -> Vista Thymeleaf con paginación y filtros básicos.
     * Por defecto usa la consulta principal (joins entre tablas).
     */
    @GetMapping
    public String showContractsReport(
            @RequestParam Map<String, String> allParams,
            Model model
    ) {
        int page = 0;
        int size = 100;

        try {
            page = Integer.parseInt(allParams.getOrDefault("page", "0"));
        } catch (NumberFormatException e) {
            model.addAttribute("error", "Número de página inválido");
            return "error";
        }

        // Filtros básicos (ajusta según lo que necesites)
        Map<String, String> filters = new HashMap<>();
        filters.put("subtype", allParams.getOrDefault("subtype", ""));
        filters.put("access_location", allParams.getOrDefault("access_location", ""));
        filters.put("building_status", allParams.getOrDefault("building_status", ""));
        filters.put("aufgabenstatus", allParams.getOrDefault("aufgabenstatus", ""));

        Map<String, Object> result = contractsService.getPaginatedContracts(filters, page, size);

        if (result.get("data") == null || ((List<?>) result.get("data")).isEmpty()) {
            model.addAttribute("warning", "No se encontraron contratos.");
        }

        model.addAttribute("data", result.get("data"));
        model.addAttribute("currentPage", result.get("currentPage"));
        model.addAttribute("totalItems", result.get("totalItems"));
        model.addAttribute("totalPages", result.get("totalPages"));
        model.addAttribute("filters", filters);

        return "contracts"; // templates/contracts.html
    }

    /**
     * GET /contracts/contracts -> Otra ruta si la necesitas.
     */
    @GetMapping("/contracts")
    public String showContractsPage(Model model) {
        return "contracts";  // templates/contracts.html
    }
    
    /**
     * POST /contracts/multiview -> Endpoint para solicitudes AJAX desde la vista que requieran datos de multiview.
     */
    @PostMapping("/multiview")
    public ResponseEntity<Map<String, Object>> getMultiViewData(@RequestBody DataTablesRequest request) {
        try {
            Map<String, Object> result = contractsService.handleMultiViewDataTablesRequest(request);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }
}
