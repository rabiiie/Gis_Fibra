package com.appfibra.Controller;

import java.util.List;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@RestController
@RequestMapping("/api/cataster")
public class CatasterProxyController {

    private static final String WFS_BASE_URL =
        "https://www.wfs.nrw.de/geobasis/wfs_nw_alkis_vereinfacht";

    private final RestTemplate rest;

    public CatasterProxyController(RestTemplateBuilder builder) {
        this.rest = builder.build();
    }

    @GetMapping("/layer")
    public ResponseEntity<String> getCatasterLayer(
            @RequestParam(defaultValue = "ave:Flurstueck") String layerName,
            @RequestParam String bbox) {

        String url = UriComponentsBuilder
            .fromHttpUrl("https://www.wfs.nrw.de/geobasis/wfs_nw_alkis_vereinfacht")
            .queryParam("service", "WFS")
            .queryParam("version", "2.0.0")
            .queryParam("request", "GetFeature")
            .queryParam("typenames", layerName)
            .queryParam("srsName", "EPSG:25832")
            .queryParam("outputFormat", "application/json")
            .queryParam("bbox", bbox + ",EPSG:25832")
            .build(false) // NO encode
            .toUriString();

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setAccept(List.of(MediaType.APPLICATION_JSON));
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            ResponseEntity<String> response = rest.exchange(url, HttpMethod.GET, entity, String.class);

            return ResponseEntity.status(response.getStatusCode())
                .contentType(MediaType.APPLICATION_JSON)
                .body(response.getBody());

        } catch (HttpClientErrorException e) {
            return ResponseEntity
                .status(e.getStatusCode())
                .body("{\"error\":\"" + e.getStatusCode() + " - " + e.getStatusText() + "\"}");
        } catch (Exception e) {
            return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("{\"error\":\"" + e.getMessage() + "\"}");
        }
    }

}
