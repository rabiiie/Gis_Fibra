package com.appfibra.Controller;

import java.util.List;

import com.appfibra.dto.ClientHierarchy;
import com.appfibra.service.GeoserverService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador para exponer la jerarquía y campos de capas desde GeoServer.
 */
@RestController
@RequestMapping("/api/geoserver")
public class GeoserverController {

    private final GeoserverService geoserverService;

    @Autowired
    public GeoserverController(GeoserverService geoserverService) {
        this.geoserverService = geoserverService;
    }

    /**
     * Devuelve la jerarquía Cliente -> Proyecto -> Villages
     * (ahora dinámicamente desde GeoServer).
     */
    @GetMapping("/hierarchy")
    public ResponseEntity<List<ClientHierarchy>> getHierarchy() {
        List<ClientHierarchy> hierarchy = geoserverService.getLayerHierarchy();
        return ResponseEntity.ok(hierarchy);
    }

    /**
     * Devuelve la lista de campos (atributos) de una capa.
     */
    @GetMapping("/fields")
    public ResponseEntity<List<String>> getLayerFields(
            @RequestParam("layer") String layerName) {
        List<String> fields = geoserverService.getLayerFields(layerName);
        return ResponseEntity.ok(fields);
    }
}
