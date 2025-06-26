package com.appfibra.Controller;
import java.util.List;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/geoserver")
public class GeoServerProxyController {

    private final RestTemplate restTemplate = new RestTemplate();

    @GetMapping("/capabilities")
    public ResponseEntity<String> getCapabilities(@RequestParam String layer) {
        try {
            String[] parts = layer.split(":");
            if (parts.length < 2) {
                return ResponseEntity.badRequest().body("Parámetro layer inválido: " + layer);
            }
            String workspace = parts[0];

            // Cambiado de /ows a /wms
            String url = "http://localhost:8080/geoserver/" + workspace + "/wms" +
                          "?service=WMS&version=1.1.1&request=GetCapabilities";

            HttpHeaders headers = new HttpHeaders();
            headers.setAccept(List.of(MediaType.APPLICATION_XML));
            HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                requestEntity,
                String.class
            );

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_XML)
                    .body(response.getBody());

        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                 .body("Error al acceder a GeoServer: " + ex.getMessage());
        }
    }
}
