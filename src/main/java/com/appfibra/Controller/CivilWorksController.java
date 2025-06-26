package com.appfibra.Controller;

import com.appfibra.service.CivilWorksService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Controller
@RequestMapping("/civil-works")
public class CivilWorksController {

    private final CivilWorksService civilWorksService;

    // Fuentes válidas permitidas (Sin toLowerCase)
    private static final Set<String> VALID_SOURCES = Set.of("Engineering", "Construction");

    public CivilWorksController(CivilWorksService civilWorksService) {
        this.civilWorksService = civilWorksService;
    }
    
    


    @GetMapping
    public String showCivilWorks(
        @RequestParam(value = "source", required = false, defaultValue = "Engineering") String source,
        @RequestParam Map<String, String> allParams,
        Model model) {

        // Validar la fuente (source) sin toLowerCase
        if (!VALID_SOURCES.contains(source)) {
            model.addAttribute("error", "Tipo de fuente inválido: " + source);
            return "error"; // Redirige a una página de error
        }

        // Manejar conversión segura de `page`
        int page = 0;
        try {
            page = Integer.parseInt(allParams.getOrDefault("page", "0"));
        } catch (NumberFormatException e) {
            model.addAttribute("error", "Número de página inválido");
            return "error";
        }

        int size = 100; // Tamaño de página fijo

        // Crear mapa de filtros
        Map<String, String> filters = new HashMap<>();
        filters.put("client", allParams.getOrDefault("client", ""));
        filters.put("project", allParams.getOrDefault("project", ""));
        filters.put("pop", allParams.getOrDefault("pop", ""));
        filters.put("dp", allParams.getOrDefault("dp", ""));

        // Llamar al servicio
        Map<String, Object> result = civilWorksService.getPaginatedData(source, filters, page, size);

        // Validar si la consulta devuelve datos
        if (result.get("data") == null || ((List<?>) result.get("data")).isEmpty()) {
            model.addAttribute("warning", "No se encontraron resultados.");
        }

        // Añadir resultados al modelo
        model.addAttribute("data", result.get("data"));
        model.addAttribute("currentPage", result.get("currentPage"));
        model.addAttribute("totalItems", result.get("totalItems"));
        model.addAttribute("totalPages", result.get("totalPages"));
        model.addAttribute("source", source);
        model.addAttribute("filters", filters);

        return "civil-works";
    }
}
